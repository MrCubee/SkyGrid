package fr.mrcubee.skygrid.generator;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import fr.mrcubee.skygrid.generator.populator.ContainerPopulator;
import fr.mrcubee.skygrid.generator.populator.SpawnerPopulator;

/**
 * Chunk generator producing a SkyGrid world, a single random block every
 * 4 blocks on all three axes, surrounded by air.
 *
 * @author MrCubee
 * @since 1.0
 */
public class SkyGridGenerator extends ChunkGenerator {

    /**
     * Resolves material names, silently skipping those absent from the running
     * server version.
     *
     * @param names material names to resolve
     * @return the materials that exist on this server
     */
    private static EnumSet<Material> byName(final String... names) {
        return Stream.of(names)
        .map(Material::matchMaterial)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
    }

    /**
     * Block entities excluded from generation: scattered over a whole SkyGrid they
     * cost server ticks and client-side rendering for no gameplay value. They stay
     * reachable as items in containers.
     */
    private static final EnumSet<Material> BAN_BLOCK_ENTITY = byName(
        // Containers / storage
        "BARREL", "DISPENSER", "DROPPER", "HOPPER",
        "CHISELED_BOOKSHELF", "DECORATED_POT", "CRAFTER", "LECTERN",

        // Furnaces & cooking
        "FURNACE", "BLAST_FURNACE", "SMOKER",

        // Redstone / mechanisms
        "COMPARATOR", "DAYLIGHT_DETECTOR", "JUKEBOX",

        // Spawners / 1.21
        //"SPAWNER", "TRIAL_SPAWNER", "VAULT",

        // Brewing / beacons / bell / enchanting
        "BREWING_STAND", "BEACON", "CONDUIT", "BELL", "ENCHANTING_TABLE",

        // Sculk
        "SCULK_SENSOR", "CALIBRATED_SCULK_SENSOR", "SCULK_SHRIEKER", "SCULK_CATALYST",

        // Bees / brushable
        "BEEHIVE", "BEE_NEST", "SUSPICIOUS_SAND", "SUSPICIOUS_GRAVEL",

        // Heads
        "SKELETON_SKULL", "WITHER_SKELETON_SKULL", "ZOMBIE_HEAD",
        "PLAYER_HEAD", "CREEPER_HEAD", "DRAGON_HEAD", "PIGLIN_HEAD",

        // Misc / technical / 26.x additions
        "CREAKING_HEART", "COPPER_GOLEM_STATUE", "END_GATEWAY",
        "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "REPEATING_COMMAND_BLOCK",
        "JIGSAW", "STRUCTURE_BLOCK"
    );

    /** Containers kept despite the ban above, few enough to be harmless, and filled by {@link ContainerPopulator}. */
    private static final EnumSet<Material> KEEP_CONTAINERS = byName("CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "COPPER_CHEST");

    /** Fluids kept despite failing isSolid(). */
    private static final EnumSet<Material> KEEP_LIQUIDS = EnumSet.of(Material.WATER, Material.LAVA);

    /** Whether the material is one of the block entities excluded above, directly or through a tag. */
    private static boolean isBannedBlockEntity(final Material material) {
        return BAN_BLOCK_ENTITY.contains(material)
            || Tag.SIGNS.isTagged(material)
            || Tag.ALL_HANGING_SIGNS.isTagged(material)
            || Tag.BANNERS.isTagged(material)
            || Tag.BEDS.isTagged(material)
            || Tag.SHULKER_BOXES.isTagged(material)
            || Tag.CAMPFIRES.isTagged(material);
    }

    /** Materials whose placement is forbidden in the world. */
    private static final EnumSet<Material> BANNED_BLOCKS = EnumSet.of(
            Material.AIR,
            Material.BEDROCK,
            Material.BEACON,
            Material.BARRIER,
            Material.DRAGON_EGG,
            Material.FIRE,
            Material.ENCHANTED_BOOK,
            Material.WRITTEN_BOOK,
            Material.TRIAL_SPAWNER,
            Material.VAULT,
            Material.END_GATEWAY,
            Material.PISTON_HEAD
    );

    /** Whether the material must never be placed, by name pattern or explicit listing. */
    private static boolean isBannedBlock(final Material material) {
        final String materialName = material.name().toUpperCase();

        return materialName.contains("COMMAND_BLOCK") // all three command block variants
            || materialName.contains("STRUCTURE") // structure block and void
            || materialName.contains("END_PORTAL") // portal and frame
            || BANNED_BLOCKS.contains(material);
    }

    /** Materials treated as items rather than placeable blocks. */
    private static final EnumSet<Material> FORCE_ITEMS = EnumSet.of(
            Material.TORCH,
            Material.REDSTONE_TORCH,
            Material.LILY_PAD,
            Material.COMPARATOR,
            Material.REDSTONE_WIRE,
            Material.REDSTONE,
            Material.NETHER_WART,
            Material.STONE_PRESSURE_PLATE,
            Material.ACACIA_DOOR,
            Material.BIRCH_DOOR,
            Material.DARK_OAK_DOOR,
            Material.IRON_DOOR,
            Material.JUNGLE_DOOR,
            Material.SPRUCE_DOOR,
            Material.IRON_DOOR,
            Material.IRON_TRAPDOOR,
            Material.LADDER,
            Material.VINE
    );

    /**
     * Whether the material should go to containers instead of being placed.
     * Covers blocks needing a support face, which would break on the first
     * neighbour update in a SkyGrid.
     */
    private static boolean isForcedItem(final Material material) {
        return Tag.DOORS.isTagged(material)
            || Tag.TRAPDOORS.isTagged(material)
            || Tag.SIGNS.isTagged(material)
            || Tag.BANNERS.isTagged(material)
            || Tag.SAPLINGS.isTagged(material)
            || Tag.BUTTONS.isTagged(material)
            || Tag.RAILS.isTagged(material)
            || Tag.WOODEN_PRESSURE_PLATES.isTagged(material)
            || Tag.STONE_PRESSURE_PLATES.isTagged(material)
            || Tag.PRESSURE_PLATES.isTagged(material)
            || Tag.BEDS.isTagged(material)
            || Tag.CORALS.isTagged(material)
            || Tag.CORAL_BLOCKS.isTagged(material)
            || Tag.CORAL_PLANTS.isTagged(material)
            || material.name().toUpperCase().contains("CORAL") // dead variants aren't in any coral tag, hence the name check
            || FORCE_ITEMS.contains(material);
    }

    /** Creatures forbidden from spawning, bosses and unused entities. */
    private static final EnumSet<EntityType> BAN_CREATURES = EnumSet.of(
            EntityType.ENDER_DRAGON,
            EntityType.GIANT
    );

    /** Materials the grid is built from, resolved once at class loading. */
    public static final Material[] BLOCKS = Arrays.stream(Material.values())
        .filter(material -> !material.isLegacy())
        .filter(material -> !material.isAir())
        .filter(material -> (material.isBlock() && material.isSolid()) || KEEP_LIQUIDS.contains(material))
        .filter(material -> !isBannedBlock(material))
        .filter(material -> !isBannedBlockEntity(material) || KEEP_CONTAINERS.contains(material))
        .filter(material -> !isForcedItem(material))
        .toArray(Material[]::new);

    /** Materials the populator can put inside generated containers. */
    public static final Material[] ITEMS = Arrays.stream(Material.values())
        .filter(material -> !material.isLegacy())
        .filter(material -> !material.isAir())
        .filter(Material::isItem)
        .filter(material -> !material.isBlock() || !material.isSolid()
                            || isForcedItem(material) || isBannedBlockEntity(material))
        .filter(material -> !isBannedBlock(material))
        .toArray(Material[]::new);

    /** Creatures a generated spawner may be assigned. */
    public static final EntityType[] CREATURES = Arrays.stream(EntityType.values())
        .filter(creature -> creature.getEntityClass() != null)
        .filter(creature -> Mob.class.isAssignableFrom(creature.getEntityClass()))
        .filter(creature -> !SkyGridGenerator.BAN_CREATURES.contains(creature))
        .toArray(EntityType[]::new);

    /** Fills generated containers with random items. */
    private final ContainerPopulator skyGridPopulator;

    /** Assigns a random creature to generated spawners. */
    private final SpawnerPopulator spawnerPopulator;

    public SkyGridGenerator() {
        this.skyGridPopulator = new ContainerPopulator();
        this.spawnerPopulator = new SpawnerPopulator();
    }

//    private void generatePlatform(final ChunkData chunkData, final int y) {
//        for (int z = 0; z < 16; z++)
//            for (int x = 0; x < 16; x++)
//                chunkData.setBlock(x, y, z, Material.GLASS);
//    }

    /** Places one block, adjusting block data that would otherwise decay or misbehave. */
    private void generateSkygridBlock(final ChunkData chunkData, final Material material, final int x, final int y, final int z) {
        final BlockData blockData = material.createBlockData();
        final Leaves leaves;
        final Piston piston;

        if (blockData == null) {
             chunkData.setBlock(x, y, z, material);
            return;
        }

        if (Tag.LEAVES.isTagged(material)) {
            leaves = (Leaves) material.createBlockData();
            leaves.setPersistent(true);
        }

        if (material == Material.PISTON) {
            piston = (Piston) material.createBlockData();
            piston.setExtended(false);
        }

        chunkData.setBlock(x, y, z, blockData);
    }

    /** Fills one horizontal layer of the chunk, one block every 4 on X and Z. */
    private void generateSkyGrid(final ChunkData chunkData, final Random random, final int y) {
        for (int z = 0; z < 16; z += 4)
            for (int x = 0; x < 16; x += 4)
                 generateSkygridBlock(chunkData, SkyGridGenerator.BLOCKS[random.nextInt(SkyGridGenerator.BLOCKS.length)], x, y, z);
    }

    /**
     * Builds the whole grid in a single pass; the other generation stages are
     * unused.
     */
    @Override
    public void generateNoise(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {
        final Random random = new Random();
//        final boolean surface = Math.abs(chunkX) <= 1 && Math.abs(chunkZ) <= 1;
        
        for (int y = 0; y < worldInfo.getMaxHeight(); y += 4) {
//            if (surface && y == worldInfo.getMaxHeight() - 4)
//                generatePlatform(chunkData, y);
//            else
//                generateSkyGrid(chunkData, random, y);
            generateSkyGrid(chunkData, random, y);
        }
    }

    @Override
    public void generateBedrock(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {
        // no bedrock layer in a SkyGrid
    }

    @Override
    public void generateCaves(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {
        // nothing to carve, everything is already air
    }

    @Override
    public void generateSurface(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {
        // no surface, the grid has no ground level
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random randomSeed) {
        return new Location(world, 0, world.getMaxHeight() - 2, 0);
    }

    /** Only grid positions can host a spawn, since everything else is air. */
    @Override
    public boolean canSpawn(World world, int x, int z) {
        return x % 4 == 0 && z % 4 == 0;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList(this.skyGridPopulator, this.spawnerPopulator);
    }

}

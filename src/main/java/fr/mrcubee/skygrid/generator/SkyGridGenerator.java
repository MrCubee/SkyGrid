package fr.mrcubee.skygrid.generator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
 * @author MrCubee
 * @since 1.0
 */
public class SkyGridGenerator extends ChunkGenerator {

    private static Stream<Material> byName(final String... names) {
        return Stream.of(names)
        .map(Material::matchMaterial)
        .filter(Objects::nonNull);
    }

    private static final List<Material> BAN_BLOCK_ENTITY = byName(
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
    ).toList();


    private static final List<Material> KEEP_CONTAINERS = byName("CHEST", "TRAPPED_CHEST").toList();

    private static boolean isBlockEntity(final Material material) {
        return BAN_BLOCK_ENTITY.contains(material)
            || Tag.SIGNS.isTagged(material)
            || Tag.ALL_HANGING_SIGNS.isTagged(material)
            || Tag.BANNERS.isTagged(material)
            || Tag.BEDS.isTagged(material)
            || Tag.SHULKER_BOXES.isTagged(material)
            || Tag.CAMPFIRES.isTagged(material);
    }

    /**
     * List of materials whose installation is prohibited in the world.
     */
    private static final List<Material> BAN_BLOCK_LIST = Arrays.asList(
            Material.AIR,
            Material.BEDROCK,
//            Material.WATER,
//            Material.LAVA,
            Material.BEACON,
            Material.BARRIER,
            Material.DRAGON_EGG,
            Material.FIRE,
            Material.ENCHANTED_BOOK,
            Material.WRITTEN_BOOK,
            Material.TRIAL_SPAWNER,
            Material.VAULT,
            Material.END_PORTAL,
            Material.COMMAND_BLOCK_MINECART
    );

    /**
     * List of materials block to be considered as items.
     */
    private static final List<Material> FORCE_ITEM_LIST = Arrays.asList(
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
            || FORCE_ITEM_LIST.contains(material);
    }

    /**
     * List of creatures prohibited from spawning.
     */
    private static final List<EntityType> BAN_CREATURE_LIST = Arrays.asList(
            EntityType.ENDER_DRAGON,
            EntityType.GIANT
    );

    /**
     * Block to build the world.
     */
    public static final Material[] BLOCKS = Arrays.stream(Material.values())
        .filter(material -> !material.isLegacy())
        .filter(material -> !material.isAir())
        .filter(Material::isBlock)
        .filter(Material::isSolid)
        .filter(material -> !BAN_BLOCK_LIST.contains(material))
        .filter(material -> !isBlockEntity(material) || KEEP_CONTAINERS.contains(material))
        .filter(material -> !isForcedItem(material))
        .toArray(Material[]::new);

    /**
     * Items to be placed in containers
     */
    public static final Material[] ITEMS = Arrays.stream(Material.values())
        .filter(material -> !material.isLegacy())
        .filter(material -> !material.isAir())
        .filter(Material::isItem)
        .filter(material -> !material.isBlock() || !material.isSolid()
                            || isForcedItem(material) || isBlockEntity(material))
        .filter(material -> !BAN_BLOCK_LIST.contains(material))
        .toArray(Material[]::new);

    /**
     * Creature allowed to spawn with mob spawner.
     */
    public static final EntityType[] CREATURES = Arrays.stream(EntityType.values())
        .filter(creature -> creature.getEntityClass() != null)
        .filter(creature -> Mob.class.isAssignableFrom(creature.getEntityClass()))
        .filter(creature -> !SkyGridGenerator.BAN_CREATURE_LIST.contains(creature))
        .toArray(EntityType[]::new);

    private final ContainerPopulator skyGridPopulator;
    private final SpawnerPopulator spawnerPopulator;

    public SkyGridGenerator() {
        this.skyGridPopulator = new ContainerPopulator();
        this.spawnerPopulator = new SpawnerPopulator();
    }

    private void generatePlatform(final ChunkData chunkData, final int y) {
        for (int z = 0; z < 16; z++)
            for (int x = 0; x < 16; x++)
                chunkData.setBlock(x, y, z, Material.GLASS);
    }

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

    private void generateSkyGrid(final ChunkData chunkData, final Random random, final int y) {
        for (int z = 0; z < 16; z += 4)
            for (int x = 0; x < 16; x += 4)
                 generateSkygridBlock(chunkData, SkyGridGenerator.BLOCKS[random.nextInt(SkyGridGenerator.BLOCKS.length)], x, y, z);
    }

    @Override
    public void generateNoise(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {
        final Random random = new Random();
        final boolean surface = Math.abs(chunkX) <= 1 && Math.abs(chunkZ) <= 1;
        
        for (int y = 0; y < worldInfo.getMaxHeight(); y += 4) {
            if (surface && y == worldInfo.getMaxHeight() - 4)
                generatePlatform(chunkData, y);
            else
                generateSkyGrid(chunkData, random, y);
        }
    }

    @Override
    public void generateBedrock(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {

    }

    @Override
    public void generateCaves(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {

    }

    @Override
    public void generateSurface(final WorldInfo worldInfo, final Random randomSeed, final int chunkX, final int chunkZ, final ChunkData chunkData) {

    }

    @Override
    public Location getFixedSpawnLocation(World world, Random randomSeed) {
        return new Location(world, 0, world.getMaxHeight() - 2, 0);
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return x % 4 == 0 && z % 4 == 0;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList(this.skyGridPopulator, this.spawnerPopulator);
    }

}

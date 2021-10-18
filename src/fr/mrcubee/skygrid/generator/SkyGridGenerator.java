package fr.mrcubee.skygrid.generator;

import fr.mrcubee.skygrid.generator.populator.ContainerPopulator;
import fr.mrcubee.skygrid.generator.populator.SpawnerPopulator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author MrCubee
 * @since 1.0
 */
public class SkyGridGenerator extends ChunkGenerator {

    /**
     * List of materials whose installation is prohibited in the world.
     */
    private static final List<Material> BAN_BLOCK_LIST = Arrays.asList(
            Material.AIR,
            Material.BEDROCK,
            Material.COMMAND,
            Material.PISTON_EXTENSION,
            Material.PISTON_MOVING_PIECE,
            Material.GLOWING_REDSTONE_ORE,
            Material.WATER,
            Material.STATIONARY_WATER,
            Material.LAVA,
            Material.STATIONARY_LAVA,
            Material.BURNING_FURNACE,
            Material.REDSTONE_LAMP_ON,
            Material.SIGN_POST,
            Material.WALL_SIGN,
            Material.IRON_DOOR_BLOCK,
            Material.ENDER_PORTAL,
            Material.PORTAL,
            Material.ENDER_PORTAL_FRAME,
            Material.BEACON,
            Material.BREWING_STAND,
            Material.BREWING_STAND_ITEM,
            Material.BARRIER,
            Material.BED_BLOCK,
            Material.DRAGON_EGG,
            Material.COMMAND_MINECART,
            Material.FIRE,
            Material.STANDING_BANNER,
            Material.WALL_BANNER,
            Material.BOOK,
            Material.ENCHANTED_BOOK,
            Material.WRITTEN_BOOK,
            Material.MONSTER_EGG
    );

    /**
     * List of materials block to be considered as items.
     */
    private static final List<Material> FORCE_ITEM_LIST = Arrays.asList(
            Material.TORCH,
            Material.REDSTONE_TORCH_OFF,
            Material.REDSTONE_TORCH_ON,
            Material.WATER_LILY,
            Material.REDSTONE_COMPARATOR,
            Material.REDSTONE_WIRE,
            Material.REDSTONE,
            Material.NETHER_WARTS,
            Material.IRON_PLATE,
            Material.STONE_PLATE,
            Material.GOLD_PLATE,
            Material.WOOD_PLATE,
            Material.TRAP_DOOR,
            Material.ACACIA_DOOR,
            Material.BIRCH_DOOR,
            Material.DARK_OAK_DOOR,
            Material.IRON_DOOR,
            Material.JUNGLE_DOOR,
            Material.SPRUCE_DOOR,
            Material.WOOD_DOOR,
            Material.WOODEN_DOOR,
            Material.IRON_DOOR,
            Material.IRON_TRAPDOOR,
            Material.SIGN,
            Material.BANNER,
            Material.LADDER,
            Material.VINE
    );

    /**
     * List of creatures prohibited from spawning.
     */
    private static final List<CreatureType> BAN_CREATURE_LIST = Arrays.asList(
            CreatureType.ENDER_DRAGON,
            CreatureType.GIANT
    );

    /**
     * Block to build the world.
     */
    public static final Material[] BLOCKS = Arrays.stream(Material.values()).filter(material ->
        material.isBlock() && material.isSolid() && !SkyGridGenerator.FORCE_ITEM_LIST.contains(material)
        && !SkyGridGenerator.BAN_BLOCK_LIST.contains(material)
    ).collect(Collectors.toList()).toArray(new Material[0]);

    /**
     * Items to be placed in containers
     */
    public static final Material[] ITEMS = Arrays.stream(Material.values()).filter(material ->
        (!material.isBlock() || !material.isSolid() || SkyGridGenerator.FORCE_ITEM_LIST.contains(material))
        && !SkyGridGenerator.BAN_BLOCK_LIST.contains(material)
    ).collect(Collectors.toList()).toArray(new Material[0]);

    /**
     * Creature allowed to spawn with mob spawner.
     */
    public static final CreatureType[] CREATURES = Arrays.stream(CreatureType.values()).filter(creature ->
        !SkyGridGenerator.BAN_CREATURE_LIST.contains(creature)
    ).collect(Collectors.toList()).toArray(new CreatureType[0]);

    private final ContainerPopulator skyGridPopulator;
    private final SpawnerPopulator spawnerPopulator;

    public SkyGridGenerator() {
        this.skyGridPopulator = new ContainerPopulator();
        this.spawnerPopulator = new SpawnerPopulator();
    }

    private void generatePlatform(byte[] bytes, int y) {
        for (int z = 0; z < 16; z++)
            for (int x = 0; x < 16; x++)
                bytes[(x * 16 + z) * 256 + y] = (byte) Material.GLASS.getId();
    }

    private void generateSurface(byte[] bytes, Random random, int y) {
        for (int z = 0; z < 16; z += 4)
            for (int x = 0; x < 16; x += 4)
                bytes[(x * 16 + z) * 256 + y] = (byte) SkyGridGenerator.BLOCKS[random.nextInt(SkyGridGenerator.BLOCKS.length)].getId();
    }

    @Override
    public byte[] generate(World world, Random random, int chunkX, int chunkZ) {
        byte[] result = new byte[16 * 16 * 256];
        boolean surface = Math.abs(chunkX) <= 1 && Math.abs(chunkZ) <= 1;

        for (int y = 0; y < world.getMaxHeight(); y += 4) {
            if (surface && y == world.getMaxHeight() - 4)
                generatePlatform(result, y);
            else
                generateSurface(result, random, y);
        }
        return result;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, world.getMaxHeight() - 3, 0);
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

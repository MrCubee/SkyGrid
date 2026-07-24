package fr.mrcubee.skygrid.generator.populator;

import fr.mrcubee.skygrid.generator.SkyGridGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
 * Populator turning generated spawners into either an empty spawner or one
 * bound to a random creature.
 *
 * @author MrCubee
 * @since 1.0
 */
public class SpawnerPopulator extends BlockPopulator {

    /**
     * Assigns a random creature to a spawner, or leaves it empty half the time.
     * Spawn count is zeroed so the spawner stays inert until a player configures
     * it.
     */
    private void setupSpawner(final Random random, final CreatureSpawner spawner) {
        spawner.setSpawnCount(0);
        spawner.setSpawnedType(random.nextBoolean() ? SkyGridGenerator.CREATURES[random.nextInt(SkyGridGenerator.CREATURES.length)] : null);
        spawner.update(true, false);
    }

    /** Scans one horizontal layer of the chunk, at the same 4-block spacing as the generator. */
    private void populateLayer(Random random, Chunk chunk, int y) {
        Block block;

        for (int z = 0; z < 16; z += 4) {
            for (int x = 0; x < 16; x += 4) {
                block = chunk.getBlock(x, y, z);
                if (block.getType() == Material.SPAWNER)
                    setupSpawner(random, (CreatureSpawner) block.getState());
            }
        }
    }

    /** Walks every grid layer of the chunk looking for spawners. */
    @Override
    public void populate(World world, Random randomSeed, Chunk chunk) {
        final Random random = new Random();

        for (int y = 0; y < world.getMaxHeight(); y += 4)
            populateLayer(random, chunk, y);
    }

}
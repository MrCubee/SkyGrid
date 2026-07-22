package fr.mrcubee.skygrid.generator.populator;

import fr.mrcubee.skygrid.generator.SkyGridGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.spawner.Spawner;

import java.util.Random;

/**
 * @author MrCubee
 * @since 1.0
 */
public class SpawnerPopulator extends BlockPopulator {

    private void populateSpawner(final Random random, final CreatureSpawner spawner) {
        spawner.setSpawnCount(0);
        spawner.setSpawnedType(random.nextBoolean() ? SkyGridGenerator.CREATURES[random.nextInt(SkyGridGenerator.CREATURES.length)] : null);
        spawner.update(true, false);
    }

    private void populateSurface(Random random, Chunk chunk, int y) {
        Block block;

        for (int z = 0; z < 16; z += 4) {
            for (int x = 0; x < 16; x += 4) {
                block = chunk.getBlock(x, y, z);
                if (block.getType() == Material.SPAWNER)
                    populateSpawner(random, (CreatureSpawner) block.getState());
            }
        }
    }

    @Override
    public void populate(World world, Random randomSeed, Chunk chunk) {
        final Random random = new Random();

        for (int y = 0; y < world.getMaxHeight(); y += 4)
            populateSurface(random, chunk, y);
    }
}
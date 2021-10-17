package fr.mrcubee.skygrid.generator.populator;

import fr.mrcubee.skygrid.generator.SkyGridGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
 * @author MrCubee
 * @since 1.0
 */
public class SpawnerPopulator extends BlockPopulator {

    private void populateSpawner(Random random, CreatureSpawner creatureSpawner) {
        creatureSpawner.setCreatureType(SkyGridGenerator.CREATURES[random.nextInt(SkyGridGenerator.CREATURES.length)]);
    }

    private void populateSurface(Random random, Chunk chunk, int y) {
        Block block;

        for (int z = 0; z < 16; z += 4) {
            for (int x = 0; x < 16; x += 4) {
                block = chunk.getBlock(x, y, z);
                if (block.getType() == Material.MOB_SPAWNER)
                    populateSpawner(random, (CreatureSpawner) block.getState());
            }
        }
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int y = 0; y < world.getMaxHeight(); y += 4)
            populateSurface(random, chunk, y);
    }
}
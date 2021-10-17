package fr.mrcubee.skygrid.generator.populator;

import fr.mrcubee.skygrid.generator.SkyGridGenerator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * @author MrCubee
 * @since 1.0
 */
public class ContainerPopulator extends BlockPopulator {

    private void populateContainer(Random random, InventoryHolder inventoryHolder) {
        int sizeInventory = random.nextInt(inventoryHolder.getInventory().getSize()) + 1;
        Material material;

        for (int i = 0; i < sizeInventory; i++) {
            material = SkyGridGenerator.ITEMS[random.nextInt(SkyGridGenerator.ITEMS.length)];
            inventoryHolder.getInventory().addItem(new ItemStack(material, 1));
        }
    }

    private void populateSurface(Random random, Chunk chunk, int y) {
        Block block;

        for (int z = 0; z < 16; z += 4) {
            for (int x = 0; x < 16; x += 4) {
                block = chunk.getBlock(x, y, z);
                if (block.getState() instanceof InventoryHolder)
                    populateContainer(random, (InventoryHolder) block.getState());
            }
        }
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int y = 0; y < world.getMaxHeight(); y += 4)
            populateSurface(random, chunk, y);
    }
}
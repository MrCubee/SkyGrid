package fr.mrcubee.skygrid;

import fr.mrcubee.skygrid.generator.SkyGridGenerator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author MrCubee
 * @since 1.0
 */
public class SkyGrid extends JavaPlugin {

    private SkyGridGenerator skyGridGenerator;

    @Override
    public void onLoad() {
        this.skyGridGenerator = new SkyGridGenerator();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return this.skyGridGenerator;
    }
}

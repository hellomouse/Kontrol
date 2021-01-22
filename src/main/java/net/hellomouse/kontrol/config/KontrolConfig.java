package net.hellomouse.kontrol.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;
import net.hellomouse.kontrol.Kontrol;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Stores all the mod configuration information.
 * Usage: KontrolConfig config = KontrolConfig.getConfig();
 *
 * @author Bowserinator
 */
public class KontrolConfig {
    public static final String CONF_NAME = "config.toml";

    private boolean electricalHeating = true;
    private boolean batteryAging = true;
    private boolean generateOres = true;

    private KontrolConfig() {}

    /**
     * Returns a KontrolConfig with data populated from the config file.
     * If config file doesn't exist it will be created with default values.
     * @return A KontrolConfig object
     */
    public static KontrolConfig getConfig() {
        Path configDir = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), Kontrol.MOD_ID);
        File file = new File(configDir.toString(), CONF_NAME);

        if (file.exists()) {
            Toml toml = new Toml().read(file);
            return toml.to(KontrolConfig.class);
        }
        else {
            KontrolConfig tmp = new KontrolConfig();
            tmp.saveConfig(file);
            return tmp;
        }
    }

    /**
     * Save values to a config
     * @param file File to save to
     */
    public void saveConfig(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            new TomlWriter().write(this, file);
        } catch (IOException e) {
            LogManager.getLogger().error("Failed to write to config at " + file.toString());
            e.printStackTrace();
        }
    }

    public boolean getElectricalHeating() { return electricalHeating; }
    public boolean getBatteryAging() { return batteryAging; }
    public boolean getGenerateOres() { return generateOres; }
}

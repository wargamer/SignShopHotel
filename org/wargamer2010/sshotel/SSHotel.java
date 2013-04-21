package org.wargamer2010.sshotel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.configUtil;
import org.wargamer2010.signshop.metrics.setupMetrics;
import org.wargamer2010.sshotel.listeners.ExpiredRentListener;
import org.wargamer2010.sshotel.listeners.SignShopListener;

public class SSHotel extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft");
    private static SSHotel instance = null;

    /**
     * Log given message at given level for SSHotel
     * @param message Message to log
     * @param level Level to log at
     */
    public static void log(String message, Level level) {
        if(!message.isEmpty())
            logger.log(level,("[SignShopHotel] " + message));
    }

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        if(!pm.isPluginEnabled("SignShop")) {
            log("SignShop is not loaded, can not continue.", Level.SEVERE);
            pm.disablePlugin(this);
            return;
        }
        pm.registerEvents(new SignShopListener(), this);
        pm.registerEvents(new ExpiredRentListener(), this);
        createDir();

        String filename = "config.yml";
        FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(this, filename);
        if(ymlThing != null) {
            configUtil.loadYMLFromJar(this, SSHotel.class, ymlThing, filename);

            SignShopConfig.setupOperations(configUtil.fetchStringStringHashMap("signs", ymlThing));
            SignShopConfig.registerErrorMessages(configUtil.fetchStringStringHashMap("errors", ymlThing));
            for(Map.Entry<String, HashMap<String, String>> entry : configUtil.fetchHasmapInHashmap("messages", ymlThing).entrySet()) {
                SignShopConfig.registerMessages(entry.getKey(), entry.getValue());
            }
        }

        SignShopConfig.addLinkable("WOODEN_DOOR", "door");
        SignShopConfig.addLinkable("IRON_DOOR", "door");
        SignShopConfig.addLinkable("IRON_DOOR_BLOCK", "door");
        SignShopConfig.addLinkable("STONE_BUTTON", "button");
        SignShopConfig.addLinkable("STONE_PLATE", "plate");
        SignShopConfig.addLinkable("WOOD_PLATE", "plate");

        if(new setupMetrics().setup(this))
            log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
        else
            log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);

        setInstance(this);
        log("Enabled", Level.INFO);
    }

    @Override
    public void onDisable() {
        log("Disabled", Level.INFO);
    }

    private void createDir() {
        if(!this.getDataFolder().exists()) {
            if(!this.getDataFolder().mkdir()) {
                log("Could not create plugin folder!", Level.SEVERE);
            }
        }
    }

    private static void setInstance(SSHotel newinstance) {
        instance = newinstance;
    }

    /**
     * Gets the instance of SSHotel
     * @return instance
     */
    public static SSHotel getInstance() {
        return instance;
    }
}

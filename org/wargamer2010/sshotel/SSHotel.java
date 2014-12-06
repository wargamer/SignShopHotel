package org.wargamer2010.sshotel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.commands.CommandDispatcher;
import org.wargamer2010.signshop.configuration.*;
import org.wargamer2010.signshop.metrics.setupMetrics;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.commandUtil;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.sshotel.commands.*;
import org.wargamer2010.sshotel.listeners.ExpiredRentListener;
import org.wargamer2010.sshotel.listeners.SignShopListener;

public class SSHotel extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft");
    private static SSHotel instance = null;
    private static CommandDispatcher commandDispatcher = new CommandDispatcher();

    private static int MaxRentsPerPerson = 0;

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
            getSettings(ymlThing);
            SignShopConfig.setupOperations(configUtil.fetchStringStringHashMap("signs", ymlThing));
            SignShopConfig.registerErrorMessages(configUtil.fetchStringStringHashMap("errors", ymlThing));
            for(Map.Entry<String, HashMap<String, String>> entry : configUtil.fetchHasmapInHashmap("messages", ymlThing).entrySet()) {
                SignShopConfig.registerMessages(entry.getKey(), entry.getValue());
            }
        }

        setupCommands();

        SignShopConfig.addLinkable("WOODEN_DOOR", "door");
        SignShopConfig.addLinkable("IRON_DOOR", "door");
        SignShopConfig.addLinkable("IRON_DOOR_BLOCK", "door");
        SignShopConfig.addLinkable("STONE_BUTTON", "button");
        SignShopConfig.addLinkable("STONE_PLATE", "plate");
        SignShopConfig.addLinkable("WOOD_PLATE", "plate");

        setupMetrics metrics = new setupMetrics(this);
        if(!metrics.isOptOut()) {
            if(metrics.setup())
                log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
            else
                log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);
        }

        fixStaleHotelRents();

        setInstance(this);
        log("Enabled", Level.INFO);
    }

    /**
     * If the server didn't propertly shutdown it is possible for rents to become corrupt
     * This method removes stale rents (i.e. rents which have a renter but no left but)
     */
    private void fixStaleHotelRents() {
        for(Seller seller : Storage.get().getSellers()) {
            if(seller == null)
                continue;
            String timeleft = RoomRegistration.getTimeLeftForRoom(seller);
            if(timeleft.equalsIgnoreCase("N/A") && RoomRegistration.getPlayerFromShop(seller) != null) {
                SignShop.log(String.format("Fixing stale rent for hotelroom at '%s'",
                        signshopUtil.convertLocationToString(seller.getSignLocation()))
                        , Level.WARNING);
                RoomRegistration.setPlayerForShop(seller, null);
            }
        }
    }

    @Override
    public void onDisable() {
        log("Disabled", Level.INFO);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        String commandName = cmd.getName().toLowerCase();
        if(!commandName.equalsIgnoreCase("signshophotel") && !commandName.equalsIgnoreCase("sshotel"))
            return true;
        return commandUtil.handleCommand(sender, cmd, commandLabel, args, commandDispatcher);
    }

    private void createDir() {
        if(!this.getDataFolder().exists()) {
            if(!this.getDataFolder().mkdir()) {
                log("Could not create plugin folder!", Level.SEVERE);
            }
        }
    }

    /**
     * Retrieves the settings from the SSHotel config file
     * This does not include the SignShop signs/messages/errors sections
     * @param ymlThing
     */
    private static void getSettings(FileConfiguration ymlThing) {
        MaxRentsPerPerson = ymlThing.getInt("MaxRentsPerPerson", 0);
    }

    private static void setInstance(SSHotel newinstance) {
        instance = newinstance;
    }

    private void setupCommands() {
        commandDispatcher.registerHandler("reload", ReloadHandler.getInstance());
        commandDispatcher.registerHandler("boot", BootHandler.getInstance());
        commandDispatcher.registerHandler("helper", HelpHandler.getInstance());
        commandDispatcher.registerHandler("", HelpHandler.getInstance());
    }

    /**
     * Gets the instance of SSHotel
     * @return instance
     */
    public static SSHotel getInstance() {
        return instance;
    }

    public static int getMaxRentsPerPerson() {
        return MaxRentsPerPerson;
    }

    /**
     * Returns the SignShopHotel Command Dispatcher
     * @return CommandDispatcher
     */
    public static CommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }
}

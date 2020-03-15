package org.wargamer2010.signshophotel;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.commands.CommandDispatcher;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.configuration.configUtil;
import org.wargamer2010.signshop.util.commandUtil;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshophotel.commands.BootHandler;
import org.wargamer2010.signshophotel.commands.HelpHandler;
import org.wargamer2010.signshophotel.commands.ReloadHandler;
import org.wargamer2010.signshophotel.listeners.ExpiredRentListener;
import org.wargamer2010.signshophotel.listeners.SignShopListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHotel extends JavaPlugin {
    private static final int B_STATS_ID = 6768;
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


        fixStaleHotelRents();

        setInstance(this);
        //Enable metrics
        if (SignShopConfig.metricsEnabled()) {
            Metrics metrics = new Metrics(this, B_STATS_ID);
            log("Thank you for enabling metrics!", Level.INFO);
        }
        log("Enabled", Level.INFO);
    }

    private static void setInstance(SSHotel newInstance) {
        instance = newInstance;
    }

    @Override
    public void onDisable() {
        log("Disabled", Level.INFO);
    }

    /**
     * If the server didn't properly shutdown it is possible for rents to become corrupt
     * This method removes stale rents (i.e. rents which have a renter but no left but)
     */
    private void fixStaleHotelRents() {
        for (Seller seller : Storage.get().getSellers()) {
            if (seller == null)
                continue;
            String timeLeft = RoomRegistration.getTimeLeftForRoom(seller);
            if (timeLeft.equalsIgnoreCase("N/A") && RoomRegistration.getPlayerFromShop(seller) != null) {
                SignShop.log(String.format("Fixing stale rent for hotel room at '%s'",
                        signshopUtil.convertLocationToString(seller.getSignLocation()))
                        , Level.WARNING);
                RoomRegistration.setPlayerForShop(seller, null);
            }
        }
    }

    private void createDir() {
        if(!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdir()) {
                log("Could not create plugin folder!", Level.SEVERE);
            }
        }
    }

    private static void getSettings(FileConfiguration ymlThing) {
        MaxRentsPerPerson = ymlThing.getInt("MaxRentsPerPerson", 0);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String commandLabel, @NotNull String[] args) {
        String commandName = cmd.getName().toLowerCase();
        if (!commandName.equalsIgnoreCase("signshophotel") && !commandName.equalsIgnoreCase("sshotel"))
            return true;
        return commandUtil.handleCommand(sender, cmd, commandLabel, args, commandDispatcher);
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

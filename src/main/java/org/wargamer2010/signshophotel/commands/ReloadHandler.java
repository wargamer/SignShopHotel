
package org.wargamer2010.signshophotel.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.wargamer2010.signshop.commands.ICommandHandler;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshophotel.SSHotel;

import java.util.logging.Level;

public class ReloadHandler implements ICommandHandler {
    private static ICommandHandler instance = new ReloadHandler();

    private ReloadHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    /**
     * Handles the reload command
     * @param command command
     * @param args arguments passed to the command
     * @param player player who started the command
     * @return true if the command was able to execute
     */
    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        if (signshopUtil.notOPForCommand(player))
            return true;
        PluginManager pm = Bukkit.getPluginManager();

        pm.disablePlugin(SSHotel.getInstance());
        pm.enablePlugin(SSHotel.getInstance());

        SSHotel.log("Reloaded", Level.INFO);
        if(player != null)
            player.sendMessage(ChatColor.GREEN + SignShopConfig.getError("reloaded", null));

        return true;
    }

}

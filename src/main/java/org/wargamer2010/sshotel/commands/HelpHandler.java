
package org.wargamer2010.sshotel.commands;

import org.wargamer2010.signshop.commands.ICommandHandler;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.commandUtil;

import java.util.LinkedList;
import java.util.List;

import static org.wargamer2010.signshop.util.commandUtil.formatAllCommands;

public class HelpHandler implements ICommandHandler {
    private static ICommandHandler instance = new HelpHandler();

    private HelpHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        commandUtil.sendToPlayerOrConsole(getAllCommands(), player);
        return true;
    }

    public static String getAllCommands() {
        List<String> commands = new LinkedList<>();
        commands.add("help~");
        commands.add("boot PLAYERNAME~(Boots the player from all rooms)");
        commands.add("boot~(Boots the player from the room you're looking at)");
        return formatAllCommands(commands, "signshophotel");
    }
}

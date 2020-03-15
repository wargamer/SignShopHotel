
package org.wargamer2010.signshophotel.commands;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.commands.ICommandHandler;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.commandUtil;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshophotel.RoomRegistration;
import org.wargamer2010.signshophotel.util.SSHotelUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class BootHandler implements ICommandHandler {
    private static ICommandHandler instance = new BootHandler();

    private BootHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    // Should use an alternative method here, but nothing seems to work right now
    private static Block getTarget(Player entity) {
        return entity.getTargetBlock(null, 200);
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        if (signshopUtil.notOPForCommand(player))
            return true;
        if (args.length == 0) {
            if (player == null) {
                commandUtil.sendToPlayerOrConsole(SignShopConfig.getError("cant_use_boot_from_console", null), player);
                return true;
            }

            Player rawPlayer = player.getPlayer();
            Block block = getTarget(rawPlayer);
            Seller seller = Storage.get().getSeller(block.getLocation());
            Map<String, String> messageParts = new LinkedHashMap<>();

            if(seller != null) {
                messageParts.put("!hotel", seller.getMisc("Hotel"));
                messageParts.put("!roomnr", seller.getMisc("RoomNr"));

                SignShopPlayer booted = SSHotelUtil.bootPlayerFromRoom(seller);

                if(booted != null) {
                    player.sendMessage(SignShopConfig.getError("booted_from_sign", messageParts));
                    booted.sendMessage(SignShopConfig.getError("you_have_been_booted_sign", messageParts));
                }
                else {
                    player.sendMessage(SignShopConfig.getError("no_player_to_boot", messageParts));
                }
            } else {
                player.sendMessage(SignShopConfig.getError("block_not_hotel_room", messageParts));
            }

        } else {
            String playerName = args[0];
            SignShopPlayer targetPlayer = PlayerIdentifier.getByName(playerName);
            if(targetPlayer.GetIdentifier().getOfflinePlayer() == null && targetPlayer.getPlayer() == null) {
                commandUtil.sendToPlayerOrConsole(SignShopConfig.getError("player_does_not_exist", null), player);
                return true;
            }

            for (Block block : RoomRegistration.getRentsForPlayer(targetPlayer)) {
                SSHotelUtil.bootPlayerFromRoom(Storage.get().getSeller(block.getLocation()));
            }

            commandUtil.sendToPlayerOrConsole(SignShopConfig.getError("booted_from_all_rooms", null), player);
            targetPlayer.sendMessage(SignShopConfig.getError("you_have_been_booted_all", null));
        }
        return true;
    }
}

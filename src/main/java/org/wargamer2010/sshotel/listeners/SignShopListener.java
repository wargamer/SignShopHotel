
package org.wargamer2010.sshotel.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.blocks.SSDoor;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.events.SSTouchShopEvent;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.sshotel.RoomRegistration;
import org.wargamer2010.sshotel.timing.RoomExpiration;
import org.wargamer2010.sshotel.util.SSHotelUtil;

public class SignShopListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;

        boolean foundHotelSign = false;
        for(String item : SignShopConfig.getBlocks(event.getOperation()))
            if (item.equalsIgnoreCase("HotelSign")) {
                foundHotelSign = true;
                break;
            }
        if (!foundHotelSign)
            return;

        Sign sign = (Sign) event.getSign().getState();
        String hotel = SSHotelUtil.trimBrackets(sign.getLine(1));

        Block bDoor = SSHotelUtil.getHotelPartFromBlockList(event.getActivatables());
        int roomNumber = RoomRegistration.registerRoom(bDoor, hotel);
        if (roomNumber < 0) {
            event.getPlayer().sendMessage(SignShopConfig.getError("already_registered", event.getMessageParts()));
            event.setCancelled(true);
            return;
        }

        event.setMiscSetting("Hotel", hotel);
        event.setMiscSetting("RoomNr", Integer.toString(roomNumber));
        event.setMiscSetting("Period", SSHotelUtil.trimBrackets(sign.getLine(2)));
        event.setMiscSetting("Price", SSHotelUtil.getNumberFromFourthLine(event.getSign()).toString());
        event.setMiscSetting("Renter", "");

        sign.setLine(2, ("Room #" + roomNumber));
        sign.update();

        SSDoor door = new SSDoor(bDoor);
        door.setOpen(false);
    }

    private boolean isOpenDoorInteraction(Action ac, Block block) {
        if(ac == Action.PHYSICAL)
            return true;
        return ac == Action.RIGHT_CLICK_BLOCK;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSTouchShopEvent(SSTouchShopEvent event) {
        if (event.isCancelled() || !isOpenDoorInteraction(event.getAction(), event.getBlock()))
            return;
        if (SSHotelUtil.doesNotContainHotelBlock(event.getShop().getOperation()))
            return;
        Seller shop = RoomRegistration.getRoomByDoor(event.getBlock());
        if(shop == null)
            return;
        SignShopPlayer renter = RoomRegistration.getPlayerFromShop(shop);
        event.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(event.getShop().getMisc("Period")));

        if((renter == null || !renter.compareTo(event.getPlayer())) && !event.getPlayer().isOp()) {
            event.setCancelled(true);
            // Sending messages on Physical interaction will cause spam
            if(event.getAction() != Action.PHYSICAL)
                event.getPlayer().sendMessage("You have not rented this room so you're not allowed to enter!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSCleanup(SSDestroyedEvent event) {
        if (event.isCancelled())
            return;
        Seller seller = event.getShop();
        if (!seller.getOperation().equals("hotel"))
            return;

        RoomExpiration roomEx = SSHotelUtil.getRoomExpirationFromSeller(seller);
        if (roomEx == null)
            return;
        SignShop.getTimeManager().removeExpirable(roomEx.getEntry());

        // SignShop can not safely assume that breaking an attachable invalidates the shop so
        // if a door is broken, we can be pretty sure the Hotel sign is useless
        if (event.getReason() == SSDestroyedEventType.attachable && itemUtil.clickedDoor(event.getBlock())) {
            Storage.get().removeSeller(event.getShop().getSign().getLocation());
        }
    }
}

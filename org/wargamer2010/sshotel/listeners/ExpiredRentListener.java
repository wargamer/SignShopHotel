
package org.wargamer2010.sshotel.listeners;

import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.blocks.SSDoor;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSExpiredEvent;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopEventHandler;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.sshotel.RoomRegistration;
import org.wargamer2010.sshotel.timing.RoomExpiration;
import org.wargamer2010.sshotel.util.SSHotelUtil;

public class ExpiredRentListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onRentExpired(SSExpiredEvent event) {
        String className = RoomExpiration.class.getName();
        if(event.getExpirable().getName().equals(className)) {
            RoomExpiration roomexpired = (RoomExpiration) event.getExpirable();
            List<Block> sellers = Storage.get().getShopsWithMiscSetting("Hotel", roomexpired.getHotel());
            for(Block block : sellers) {
                Seller seller = Storage.get().getSeller(block.getLocation());
                if(seller != null) {
                    if(!seller.hasMisc("RoomNr") || !seller.getMisc("RoomNr").equals(Integer.toString(roomexpired.getRoomNr())))
                        continue;
                    SignShopArguments ssArgs = new SignShopArguments(seller, RoomRegistration.getPlayerFromShop(seller), SignShopArgumentsType.Run);
                    SignShopEventHandler.dispatchEvent(ssArgs, event, seller.getOperation());

                    SSHotelUtil.bootPlayerFromRoom(seller);
                }
            }
        }
    }

}

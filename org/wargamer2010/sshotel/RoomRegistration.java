
package org.wargamer2010.sshotel;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.timing.IExpirable;
import org.wargamer2010.signshop.util.SSTimeUtil;
import org.wargamer2010.sshotel.timing.RoomExpiration;
import org.wargamer2010.sshotel.util.SSHotelUtil;

public class RoomRegistration {
    private RoomRegistration() {

    }

    public static int registerRoom(Block door, String hotel) {
        Storage storage = Storage.get();
        if(!storage.getShopsByBlock(door).isEmpty())
            return -1;

        return (storage.getShopsWithMiscSetting("Hotel", hotel).size() + 1);
    }

    public static int getRoomNumber(Seller seller) {
        if(seller.getMisc().containsKey("RoomNr")) {
            try {
                return Integer.parseInt(seller.getMisc().get("RoomNr"));
            } catch(NumberFormatException ex) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public static Seller getRoomByDoor(Block door) {
        List<Seller> sellers = Storage.get().getShopsByBlock(door);
        if(sellers.isEmpty())
            return null;
        else
            return sellers.get(0);
    }

    public static String getPlayerFromShop(Seller seller) {
        if(seller.getMisc().containsKey("Renter")) {
            return seller.getMisc().get("Renter");
        }
        return "";
    }

    public static void setPlayerForShop(Seller seller, String player) {
        seller.getMisc().put("Renter", player);

        Sign sign = (Sign) seller.getSign().getState();

        if(!player.isEmpty())
            sign.setLine(3, (ChatColor.DARK_GREEN + player));
        else
            sign.setLine(3, seller.getMisc().get("Price"));
        sign.update();
    }

    public static int getAmountOfRentsForPlayer(String player) {
        return (Storage.get().getShopsWithMiscSetting("Renter", player).size());
    }

    /**
     * Returns a string representation of the time left for the current Hotel room
     * Or returns N/A if the room is not rented out
     *
     * @param seller
     * @return String representation of the time left
     */
    public static String getTimeLeftForRoom(Seller seller) {
        if(seller == null)
            return "N/A";
        RoomExpiration roomex = SSHotelUtil.getRoomExpirationFromSeller(seller);
        if(roomex == null)
            return "N/A";
        int left = SignShop.getTimeManager().getTimeLeftForExpirable(roomex.getEntry());
        if(left == -1)
            return "N/A"; // TODO: Handle this somewhere else?
        return SSTimeUtil.parseTime(left);
    }
}

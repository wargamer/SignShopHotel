
package org.wargamer2010.signshophotel.util;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.blocks.SSDoor;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshophotel.SSHotel;
import org.wargamer2010.signshophotel.RoomRegistration;
import org.wargamer2010.signshophotel.timing.RoomExpiration;

import java.util.List;
import java.util.logging.Level;

public class SSHotelUtil {

    private SSHotelUtil() {

    }

    public static Block getHotelPartFromBlockList(List<Block> blockList) {
        Block bDoor = null;
        for (Block bBlock : blockList) {
            if (bBlock.getType().toString().contains("_DOOR"))
                bDoor = bBlock;
        }
        return bDoor;
    }

    public static RoomExpiration getRoomExpirationFromSeller(Seller seller) {
        if (doesNotContainHotelBlock(seller.getOperation()))
            return null;

        String hotel = seller.getMisc("Hotel");
        int roomnr;
        try {
            roomnr = Integer.parseInt(seller.getMisc("RoomNr"));
        } catch (NumberFormatException ex) {
            SSHotel.log("Could not parse RoomNr: " + seller.getMisc("RoomNr"), Level.WARNING);
            return null;
        }
        return new RoomExpiration(hotel, roomnr);
    }

    public static String getPrintablePeriod(String pPeriod) {
        HotelLength period = getPeriodFromString(pPeriod);
        if (!period.valid)
            return "";

        String sAmount = Integer.toString(period.length);
        String append = (period.length > 1 ? "s" : "");

        switch (period.unit) {
            case w:
                return (sAmount + " week" + append);
            case d:
                return (sAmount + " day" + append);
            case h:
                return (sAmount + " hour" + append);
            case m:
                return (sAmount + " minute" + append);
            case s:
                return (sAmount + " second" + append);
        }
        return "";
    }

    public static boolean doesNotContainHotelBlock(String operation) {
        List<String> temp = SignShopConfig.getBlocks(operation);
        if (temp == null)
            return true;
        for (String op : temp)
            if (op.equalsIgnoreCase("hotelsign"))
                return false;
        return true;
    }

    private static boolean isInt(String testInt) {
        try {
            Integer.parseInt(testInt);
            return true;
        } catch (NumberFormatException ignored) {
        }

        return false;
    }

    /**
     * Boots the current renter from the room indicated by the seller
     *
     * @param seller Shop
     * @return Player who was booted or null if there was not renter
     */
    public static SignShopPlayer bootPlayerFromRoom(Seller seller) {
        if (RoomRegistration.getPlayerFromShop(seller) == null)
            return null;
        SignShopPlayer currentRenter = RoomRegistration.getPlayerFromShop(seller);
        RoomRegistration.setPlayerForShop(seller, null);

        for (Block door : seller.getActivatables()) {
            if (itemUtil.clickedDoor(door))
                (new SSDoor(door)).setOpen(false);
        }

        return currentRenter;
    }

    private enum Period {
        w,
        d,
        h,
        m,
        s,
    }

    private static HotelLength getPeriodFromString(String pPeriod) {
        HotelLength returnValue = new HotelLength();
        StringBuilder number_b = new StringBuilder(pPeriod.length());
        StringBuilder period_b = new StringBuilder(pPeriod.length());
        for (int i = 0; i < pPeriod.length(); i++) {
            String testInt = "";
            testInt += pPeriod.charAt(i);
            if (isInt(testInt))
                number_b.append(pPeriod.charAt(i));
            else
                period_b.append(pPeriod.charAt(i));
        }
        try {
            returnValue.unit = Period.valueOf(period_b.toString().trim());
        } catch(IllegalArgumentException ex) {
            return returnValue;
        }

        if(!isInt(number_b.toString().trim()))
            return returnValue;
        returnValue.length = Integer.parseInt(number_b.toString().trim());
        returnValue.valid = true;
        return returnValue;
    }

    public static int getPeriod(String pPeriod) {
        HotelLength period = getPeriodFromString(pPeriod);
        if(!period.valid)
            return -1;

        switch(period.unit) {
            case w:
                return (period.length * 7 * 24 * 60 * 60);
            case d:
                return (period.length * 24 * 60 * 60);
            case h:
                return (period.length * 60 * 60);
            case m:
                return (period.length * 60);
            case s:
                return period.length;
        }
        return -1;
    }

    public static String trimBrackets(String toTrim) {
        String temp = toTrim.trim();
        temp = temp.replace("[", "");
        temp = temp.replace("]", "");
        return temp;
    }

    public static Double getNumberFromFourthLine(Block bSign) {
        Sign sign = (Sign)bSign.getState();
        String line = sign.getLine(3);
        return economyUtil.parsePrice(line);
    }

    private static class HotelLength {
        public int length = -1;
        public Period unit = null;
        public boolean valid = false;
    }

}

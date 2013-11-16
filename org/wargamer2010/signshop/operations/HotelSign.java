package org.wargamer2010.signshop.operations;

import java.util.logging.Level;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.sshotel.RoomRegistration;
import org.wargamer2010.sshotel.SSHotel;
import org.wargamer2010.sshotel.timing.RoomExpiration;
import org.wargamer2010.sshotel.util.SSHotelUtil;

public class HotelSign implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Block bDoor = SSHotelUtil.getHotelPartFromBlocklist(ssArgs.getActivatables().get());
        if(bDoor == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("need_door", ssArgs.getMessageParts()));
            return false;
        }

        Sign sign = (Sign)ssArgs.getSign().get().getState();
        String fourthline = SSHotelUtil.trimBrackets(sign.getLine(2));
        if(fourthline == null || SSHotelUtil.getPeriod(fourthline) == -1) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("invalid_time", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.getPrice().set(SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get()));
        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(((Sign)ssArgs.getSign().get().getState()).getLine(2)));

        String secondline = sign.getLine(1);
        if(secondline == null || SSHotelUtil.trimBrackets(secondline).isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("invalid_hotel", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(sign.getLine(2)));

        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        Block door = SSHotelUtil.getHotelPartFromBlocklist(ssArgs.getActivatables().get());
        if(door == null)
            return false;
        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(ssArgs.miscSettings.get("Period")));
        ssArgs.setMessagePart("!hotel", ssArgs.miscSettings.get("Hotel"));
        ssArgs.setMessagePart("!roomnr", ssArgs.miscSettings.get("RoomNr"));
        SignShopPlayer player = ssArgs.getPlayer().get();

        if(SSHotel.getMaxRentsPerPerson() != 0 && !player.isOp()) {
            if(RoomRegistration.getAmountOfRentsForPlayer(player.getName()) >= SSHotel.getMaxRentsPerPerson()) {
                ssArgs.setMessagePart("!maxrents", Integer.toString(SSHotel.getMaxRentsPerPerson()));
                player.sendMessage(SignShopConfig.getError("max_rents_reached", ssArgs.getMessageParts()));
                return false;
            }
        }

        if(RoomRegistration.getRoomByDoor(door) == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_door", ssArgs.getMessageParts()));
            return false;
        }

        Seller seller = Storage.get().getSeller(ssArgs.getSign().get().getLocation());
        String renter = RoomRegistration.getPlayerFromShop(seller);
        ssArgs.setMessagePart("!timeleft", RoomRegistration.getTimeLeftForRoom(seller));

        if(renter.equals(ssArgs.getPlayer().get().getName())) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_rented_self_timeleft", ssArgs.getMessageParts()));
            return false;
        } else if(!renter.isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_rented_other_timeleft", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.getPrice().set(SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get()));

        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Block door = SSHotelUtil.getHotelPartFromBlocklist(ssArgs.getActivatables().get());
        if(door == null)
            return false;
        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(ssArgs.miscSettings.get("Period")));
        ssArgs.setMessagePart("!hotel", ssArgs.miscSettings.get("Hotel"));
        ssArgs.setMessagePart("!roomnr", ssArgs.miscSettings.get("RoomNr"));

        String playername = ssArgs.getPlayer().get().getName();
        Seller seller = Storage.get().getSeller(ssArgs.getSign().get().getLocation());

        Float fPrice = SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get());
        seller.getMisc().put("Price", fPrice.toString());
        ssArgs.getPrice().set(fPrice);

        RoomRegistration.setPlayerForShop(seller, playername);

        Integer period = SSHotelUtil.getPeriod(seller.getMisc().get("Period"));

        RoomExpiration roomex = SSHotelUtil.getRoomExpirationFromSeller(seller);
        if(roomex == null)
            return false;

        SignShop.getTimeManager().addExpirable(roomex, period);

        return true;
    }
}

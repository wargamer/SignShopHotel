package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshophotel.RoomRegistration;
import org.wargamer2010.signshophotel.SSHotel;
import org.wargamer2010.signshophotel.timing.RoomExpiration;
import org.wargamer2010.signshophotel.util.SSHotelUtil;

public class HotelSign implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Block bDoor = SSHotelUtil.getHotelPartFromBlockList(ssArgs.getActivatables().get());
        if (bDoor == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("need_door", ssArgs.getMessageParts()));
            return false;
        }

        Sign sign = (Sign) ssArgs.getSign().get().getState();
        String fourthLine = SSHotelUtil.trimBrackets(sign.getLine(2));
        if (fourthLine == null || SSHotelUtil.getPeriod(fourthLine) == -1) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("invalid_time", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.getPrice().set(SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get()));
        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(((Sign) ssArgs.getSign().get().getState()).getLine(2)));

        String secondLine = sign.getLine(1);
        if (secondLine == null || SSHotelUtil.trimBrackets(secondLine).isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("invalid_hotel", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(sign.getLine(2)));

        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        Block door = SSHotelUtil.getHotelPartFromBlockList(ssArgs.getActivatables().get());
        if(door == null)
            return false;
        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(ssArgs.miscSettings.get("Period")));
        ssArgs.setMessagePart("!hotel", ssArgs.miscSettings.get("Hotel"));
        ssArgs.setMessagePart("!roomnr", ssArgs.miscSettings.get("RoomNr"));
        SignShopPlayer player = ssArgs.getPlayer().get();

        if(SSHotel.getMaxRentsPerPerson() != 0 && !player.isOp()) {
            if(RoomRegistration.getAmountOfRentsForPlayer(player) >= SSHotel.getMaxRentsPerPerson()) {
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
        SignShopPlayer renter = RoomRegistration.getPlayerFromShop(seller);
        ssArgs.setMessagePart("!timeleft", RoomRegistration.getTimeLeftForRoom(seller));

        if(renter != null)
            ssArgs.getPrice().set(economyUtil.parsePrice(seller.getMisc("Price")));
        else
            ssArgs.getPrice().set(SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get()));

        if(renter != null && renter.compareTo(ssArgs.getPlayer().get())) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_rented_self_timeleft", ssArgs.getMessageParts()));
            return false;
        } else if(renter != null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_rented_other_timeleft", ssArgs.getMessageParts()));
            return false;
        }


        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Block door = SSHotelUtil.getHotelPartFromBlockList(ssArgs.getActivatables().get());
        if(door == null)
            return false;
        ssArgs.setMessagePart("!renttime", SSHotelUtil.getPrintablePeriod(ssArgs.miscSettings.get("Period")));
        ssArgs.setMessagePart("!hotel", ssArgs.miscSettings.get("Hotel"));
        ssArgs.setMessagePart("!roomnr", ssArgs.miscSettings.get("RoomNr"));

        Seller seller = Storage.get().getSeller(ssArgs.getSign().get().getLocation());

        Double fPrice = SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get());
        seller.addMisc("Price", fPrice.toString());
        ssArgs.getPrice().set(fPrice);

        RoomRegistration.setPlayerForShop(seller, ssArgs.getPlayer().get());

        Integer period = SSHotelUtil.getPeriod(seller.getMisc("Period"));

        RoomExpiration roomEx = SSHotelUtil.getRoomExpirationFromSeller(seller);
        if (roomEx == null)
            return false;

        SignShop.getTimeManager().addExpirable(roomEx, period);

        return true;
    }
}

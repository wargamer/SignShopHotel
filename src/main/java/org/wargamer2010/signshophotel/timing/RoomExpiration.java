
package org.wargamer2010.signshophotel.timing;

import org.wargamer2010.signshop.timing.IExpirable;

import java.util.HashMap;
import java.util.Map;

public class RoomExpiration implements IExpirable {
    private Integer roomNr = -1;
    private String hotel = "";

    public RoomExpiration() {

    }

    public RoomExpiration(String pHotel, Integer pRoom) {
        hotel = pHotel;
        roomNr = pRoom;
    }

    @Override
    public String getName() {
        return RoomExpiration.class.getCanonicalName();
    }

    @Override
    public boolean parseEntry(Map<String, String> entry) {
        if(entry.containsKey("roomnr")) {
            try {
                roomNr = Integer.parseInt(entry.get("roomnr"));
                if (entry.containsKey("hotel")) {
                    hotel = entry.get("hotel");
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    @Override
    public Map<String, String> getEntry() {
        Map<String, String> entry = new HashMap<>();
        entry.put("hotel", hotel);
        entry.put("roomnr", roomNr.toString());
        return entry;
    }

    public Integer getRoomNr() {
        return roomNr;
    }

    public String getHotel() {
        return hotel;
    }
}

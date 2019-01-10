package com.skyerzz.juggernaut.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Player Spawn Location in a map
 * Created by sky on 22-10-2018.
 */
public class PlayerLocation {

    private double x, y, z;
    private Location loc;
    private int state = 0; //0 = unused, 1 = in use, 2 = has been used this game

    public PlayerLocation(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
        loc = new Location(Bukkit.getWorlds().get(0), x, y, z);
    }

    /**
     * Uses the location
     * @return Location of this playerLocation
     */
    public Location use(){
        state = 1; return loc;
    }

    /**
     * Destroys the location
     * @return Location of this playerLocation
     */
    public Location destroy(){
        state = 2; return loc;
    }

    /**
     * Checks if this location is available
     * @return True if its available.
     */
    public boolean isAvailable(){ return state==0; }

    /**
     * Gets this Location
     * @return Location of this PlayerLocation
     */
    public Location getLoc(){
        return loc;
    }

    @Override
    public String toString(){
        return "PlayerLocation " + x + ", " + y + ", " + z + ". State: " + state;
    }
}

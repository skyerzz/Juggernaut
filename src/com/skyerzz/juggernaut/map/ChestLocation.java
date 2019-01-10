package com.skyerzz.juggernaut.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Location of a chest in a map
 * Created by sky on 22-9-2018.
 */
public class ChestLocation {

    private double x, y, z;
    private Location loc;
    private int state = 0; //0 = unused, 1 = in use, 2 = has been used this game

    public ChestLocation(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
        loc = new Location(Bukkit.getWorlds().get(0), x, y, z);
    }

    /**
     * Uses this chestlocation in the map
     * @return Location of this chest
     */
    public Location use(){
        state = 1; return loc;
    }

    /**
     * Destroys this chestlocation in the map
     * @return Location of this chest
     */
    public Location destroy(){
        state = 2; return loc;
    }

    /**
     * Checks if this chestLocation is still available
     * @return True if available, else false
     */
    public boolean isAvailable(){ return state==0; }
}

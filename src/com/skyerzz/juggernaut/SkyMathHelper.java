package com.skyerzz.juggernaut;

import org.bukkit.Location;

/**
 * Helperclass for math.
 * Created by sky on 17-11-2018.
 */
public class SkyMathHelper {

    /**
     * Checks if a location is within the radius of another location
     * @param central Central point location
     * @param otherLocation the Location to check if its within the radius
     * @param radius Radius to check for
     * @param limitY True if Y different plays a factor, false if it doesnt
     * @return boolean True if its within the radius, otherwise False
     */
    public static boolean isInRadius(Location central, Location otherLocation, double radius, boolean limitY){
        double dx = Math.abs(otherLocation.getX() - central.getX());
        double dy = Math.abs(otherLocation.getZ() - central.getZ());
        if(limitY && Math.abs(central.getY() - otherLocation.getY()) > 1){
            //we check directly if y is higher or lower than the location plus or minus the radius. if it is, its not within reach.
            //TODO make this a real sphere some day. Low priority.
            return false;
        }

        //some circle math
        if(dx>radius || dy>radius){
            return false;
        }
        if(dx+dy<=radius){
            return true;
        }

        if((Math.pow(dx, 2) + Math.pow(dy, 2) <= Math.pow(radius, 2))){
            return true;
        }
        return false;
    }
}

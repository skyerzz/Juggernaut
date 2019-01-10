package com.skyerzz.juggernaut.map;

import com.skyerzz.juggernaut.map.part.MapPart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * a MapPiece with a mapPart as type, including location in the world.
 * Created by sky on 22-9-2018.
 */
public class MapPiece {

    private MapPart mapPart;
    private int startX,startY,startZ;
    private ArrayList<ChestLocation> chestLocations = new ArrayList<>();
    private ArrayList<PlayerLocation> playerLocations = new ArrayList<>();

    public MapPiece(MapPart part, int x, int y, int z){
        this.mapPart = part;
        this.startX = x;
        this.startY = y;
        this.startZ = z;
    }

    /**
     * Initialises the mappart.
     */
    public void initialise(){
        boolean[][] part = mapPart.getBlocksfilled();
        System.out.println("Initialising part at " + startX + ", " + startY + ", " + startZ + " with length " + part.length + ", " + part[0].length); //log
        for(int xPart = 0; xPart < part.length; xPart++){
            for(int zPart = 0; zPart < part[0].length; zPart++){
                if(part[xPart][zPart]){
                    //this block of the mapPart is filled in, get the real world coordinates
                    int x = startX - (xPart*MapPart.standardSize);
                    int z = startZ + (zPart*MapPart.standardSize);

                    //go over every block in this place
                    for(int dX = 0; dX < MapPart.standardSize; dX++){
                        for(int y = startY; y < 128; y++){
                            for(int dZ = 0; dZ < MapPart.standardSize; dZ++){

                                Block b = new Location(Bukkit.getWorlds().get(0), x-dX, y, z+dZ).getBlock();

                                //if its a sign, read and process it.
                                if(b.getState() instanceof  Sign){
                                    Sign s = (Sign) b.getState();
                                    processSign(s, x-dX, y, z+dZ);
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes a sign inside of a mapPiece
     * @param s Sign to process
     * @param x x coordinate of the Sign
     * @param y y coordinate of the Sign
     * @param z z coordinate of the Sign
     */
    private void processSign(Sign s, int x, int y, int z){
        if(s.getLine(1).equals("chest-spawn")){
            //we found a chest spawn location, index it!
            chestLocations.add(new ChestLocation(x+0.5d, y, z+0.5d));
            new Location(Bukkit.getWorlds().get(0), x, y, z).getBlock().setType(Material.AIR); //set sign to air
        }else if(s.getLine(1).equals("player-spawn")){
            //we found a player spawn location, index it!
            playerLocations.add(new PlayerLocation(x+0.5d, y, z+0.5d));
            new Location(Bukkit.getWorlds().get(0), x, y, z).getBlock().setType(Material.AIR); //set sign to air
        }
    }

    /**
     * Gets all possible chest locations in this mapPiece
     * @return ArrayList of ChestLocation
     */
    public ArrayList<ChestLocation> getChestLocations(){
        return chestLocations;
    }

    /**
     * Gets a random unoccupied chestlocation in this MapPiece, if any exist.
     * @return an unoccupied ChestLocation, or, if none exist, null.
     */
    public ChestLocation getRandomChestLocation(){
        ArrayList<ChestLocation> locs = (ArrayList<ChestLocation>) chestLocations.clone(); //copy the chestLocations
        Collections.shuffle(locs); //give it a good shuffle so we're random
        while(locs.size()>0){ //for as long as chests exist, try this.
           ChestLocation chestLocation = locs.remove(0);//get & remove the first chestlocation in the queue
            if(chestLocation.isAvailable()){
                //its available, return it!
                return chestLocation;
            }
        }
        //no available chestlocation was found at this point.
        return null;
    }

    /**
     * Gets a random unoccupied PlayerLocation in this MapPiece, if any exist.
     * @return an unoccupied PlayerLocation, or, if none exist, null.
     */
    public PlayerLocation getRandomPlayerLocation(){
        ArrayList<PlayerLocation> locs = (ArrayList<PlayerLocation>) playerLocations.clone();//copy the playerLocations
        Collections.shuffle(locs); //shuffle
        while(locs.size()>0){ //for as long as player spawn locations exist, try this
           PlayerLocation playerLocation = locs.remove(0); //get &remove the first location in the queue
            if(playerLocation.isAvailable()){
                //its available, return it!
                return playerLocation;
            }
        }
        //no available locations were found.
        return null;
    }
}

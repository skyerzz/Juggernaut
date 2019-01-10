package com.skyerzz.juggernaut.map;

import java.util.ArrayList;
import java.util.Random;

/**
 * Holds the Map
 * Created by sky on 22-9-2018.
 */
public class Map {

    public ArrayList<MapPiece> mapPieces = new ArrayList<>();

    public Map(){

    }

    /**
     * Adds a piece to this map
     * @param part MapPiece to add
     */
    public void addMapPiece(MapPiece part){
        part.initialise();
        this.mapPieces.add(part);
    }

    /**
     * Gets a random usable ChestLocation on the map
     * @return ChestLocation of any usable chest location on the map
     */
    public ChestLocation getRandomChestLocation(){
        Random r = new Random();
        MapPiece piece = mapPieces.get(r.nextInt(mapPieces.size()));
        ChestLocation chestLoc = piece.getRandomChestLocation();
        if(chestLoc==null){
            return getRandomChestLocation(); //failsafe
        }else{
            return chestLoc;
        }
    }

    /**
     * Gets a random usable PlayerLocation
     * @return PlayerLocation of any usable player location on the map
     */
    public PlayerLocation getRandomPlayerLocation(){
        Random r = new Random();
        MapPiece piece = mapPieces.get(r.nextInt(mapPieces.size()));
        PlayerLocation playerLoc = piece.getRandomPlayerLocation();
        if(playerLoc==null){
            return getRandomPlayerLocation(); //failsafe
        }else{
            return playerLoc;
        }
    }
}

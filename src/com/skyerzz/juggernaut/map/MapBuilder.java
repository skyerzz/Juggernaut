package com.skyerzz.juggernaut.map;

import com.skyerzz.juggernaut.map.part.MapPart;
import com.skyerzz.juggernaut.map.part.MapPartCopier;

import java.util.ArrayList;
import java.util.Random;

/**
 * Class to help build a Map
 * Created by sky on 4-9-2018.
 */
public class MapBuilder {

    private final String prefix = "[Jugg] [MapGen] ";

    private final int startx;
    private final int starty;
    private final int startz;
    private final int maxBlocks;
    public boolean[][] area;
    int curPosX = 0, curPosY = 0;
    private Map map;

    public MapBuilder(int startx, int starty, int startz, int maxBlocks){
        this.startx = startx+maxBlocks*MapPart.standardSize; //get top-left corner
        this.starty = starty;
        this.startz = startz;
        this.maxBlocks = maxBlocks;
        area = new boolean[maxBlocks][maxBlocks];
        map = new Map();
    }

    /**
     * Starts building the map
     */
    public void startBuild(){

        //try a maximum amount of times before skipping this spot and going to the next
        int maxPieceTries = 8;

        //while the map is not full and we're still inside the map on our X&Y coords
        while(!isFull() && (curPosX>=0 && curPosY>=0)){
            if(area[curPosX][curPosY]==true){
                //it was already filled!
                increasePosition();
                continue;
            }

            //this spot can still harbour a piece, lets try!
            for(int i = 0; i < maxPieceTries; i++){
                MapPart part = MapPart.getRandomPart();
                if(part.getType() != MapPart.Type.PIECE){
                    i--;
                    continue; //we dont want non-piece parts in here.
                }

                //get a new copier for this part
                MapPartCopier copier = new MapPartCopier(part);
                if(copier.doesFit(area, curPosX, curPosY)){
                    System.out.println(prefix + "piece fits! Copying piece " + part.name() + " to " + curPosX + "," + curPosY); //log
                    //it fits in the board, so lets put it in and build it
                    area = copier.putInBoard(area, curPosX, curPosY);
                    copier.build(startx, starty, startz, curPosX, curPosY);

                    //add to map
                    map.addMapPiece(new MapPiece(part, startx - (curPosX*MapPart.standardSize),  starty, startz + (curPosY*MapPart.standardSize)));
                    break; //no need to continue as we have no filled this spot.
                }
            }

            increasePosition();

        }

        //we tried the entire map, lets put in some fillers on the spots we couldnt fit anything
        fillRemainingFillers();
    }

    /**
     * Fills the remainder of the map with filler pieces
     */
    private void fillRemainingFillers(){
        System.out.println(prefix+" using fillers to fill the board..."); //log

        //get a list of all fillers
        ArrayList<MapPart> fillers = new ArrayList<>();
        for(MapPart part: MapPart.values()){
            if(part.getType()== MapPart.Type.FILLER){
                fillers.add(part);
            }
        }

        //move through the board to find a spot thats still empty.
        curPosX = 0;
        curPosY = 0;
        Random r = new Random();
        while((curPosX>=0 && curPosY>=0)) {
            if (area[curPosX][curPosY] == true || (curPosX==0 || curPosY == 0 || curPosX==area.length-1 || curPosY==area.length-1)) {
                //it was already full or its an edge, both cases we dont want a filler here.
                increasePosition();
                continue;
            }

            //get a random filler and copy it in.
            MapPart part = fillers.get(r.nextInt(fillers.size()-1));
            MapPartCopier copier = new MapPartCopier(part);
            if(copier.doesFit(area, curPosX, curPosY)){ //it should always fit, safety check.
                //copy it in the board and build it in the world
                area = copier.putInBoard(area, curPosX, curPosY);
                copier.build(startx, starty, startz, curPosX, curPosY);

                //add to map
                map.addMapPiece(new MapPiece(part, startx - (curPosX*MapPart.standardSize),  starty, startz + (curPosY*MapPart.standardSize)));
            }

            increasePosition();
        }

        System.out.println(prefix + "Done filling in!"); //log

        //create walls around the map
        createWalls();
    }

    /**
     * Creates walls around the map
     */
    private void createWalls(){
        System.out.println(prefix + "Creating walls..."); //log

        //TODO currently there's only one wall part. If there are more in the future, change this to the same implementation as the fillers.
        MapPart part = MapPart.WALL_ONE_SOUTH;

        //get a new copier and go through the entire map.
        MapPartCopier copier = new MapPartCopier(part);
        curPosX = 0;
        curPosY = 0;
        while((curPosX>=0 && curPosY>=0)) {
            if((curPosX!=0 && curPosX!=area.length-1) && (curPosY!=0 && curPosY!=area.length-1)){
                //we're not at an edge space
                increasePosition();
                continue;
            }
            if(!area[curPosX][curPosY]){
                //area wasnt filled in yet, lets give it a wall!
                copier.build(startx, starty, startz, curPosX, curPosY);
            }else{
                //build around the map, extending the reahc by 1 chunk
                if(curPosX==0){
                    System.out.println("Creating wall for X0 and Y" + curPosY);
                    copier.build(startx, starty, startz, -1, curPosY);
                }
                if(curPosX==area.length-1){
                    System.out.println("Creating wall for XMAX and Y" + curPosY);
                    copier.build(startx, starty, startz, area.length, curPosY);
                }
                if(curPosY==0){
                    System.out.println("Creating wall for X" + curPosX + " and Y0");
                    copier.build(startx, starty, startz, curPosX, -1);
                }
                if(curPosY==area.length-1){
                    System.out.println("Creating wall for X" + curPosX + " and YMAX");
                    copier.build(startx, starty, startz, curPosX, area.length);
                }
            }
            increasePosition();
        }
        System.out.println(prefix + "Done creating walls!"); //log

    }

    /**
     * Increases the current position inside the map
     */
    private void increasePosition(){
        if(++curPosY>=maxBlocks){ //increase, if its higher than the size, set to 0 and increase X instead
            curPosY = 0;
            if(++curPosX >= maxBlocks){ //if its higher than the size, we hit our endpoint.
                curPosX=-1;
                curPosY=-1;
            }
        }
    }

    /**
     * Checks if the board is filled
     * @return True if its filled, otherwise False
     */
    public boolean isFull(){
        for(boolean[] arr: area){
            for(boolean x: arr){
                if(!x){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the current Map
     * @return Map map.
     */
    public Map getMap(){
        return map;
    }
}

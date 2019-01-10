package com.skyerzz.juggernaut.map.part;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.util.Random;

/**
 * Copies a MapPart to another location
 * Created by sky on 4-9-2018.
 */
public class MapPartCopier {

    /** MapPart to copy */
    private MapPart part;

    public MapPartCopier(MapPart part){
        this.part = part;
    }

    /**
     * Checks if the piece fits the spot in the map
     * @param area 2D array representing the map
     * @param x X location of the piece startpoint
     * @param y Y location of the piece startpoint
     * @return True if it fits, else False.
     */
    public boolean doesFit(boolean[][] area, int x, int y){
        int xHeight = part.getNorthchunks();
        int yLength = part.getEastchunks();
        boolean[][] blocksFilled = part.getBlocksfilled();
        //System.out.println("[jugg] Doesfit Rotation " + 0 + " for piece " + part.name() +" with area " + area.length + " - " + area[x].length + " at X:Y " + x + ":" + y + " and max X:Y " + xHeight + " : " + yLength);

        if(area.length < x + xHeight || area.length < y + yLength ){//not including as the current position is already one of the lengths too.
            return false; //it falls outside of the map
        }

        //check if any of the map spots already have a piece in them. If so, return false.
        for(int xBlock = 0; xBlock < xHeight; xBlock++){
            for(int yBlock = 0; yBlock<yLength; yBlock++){
                if(blocksFilled[xBlock][yBlock]){
                    if(area[x+xBlock][y+yBlock]){
                        return false;
                    }
                }
            }
        }

        //nothing holds back this piece from this map position.
        return true;
    }

    /**
     * Puts the piece inside the board at the given location
     * @param area 2D array of the current board
     * @param x x startlocation of the piece
     * @param y y startlocation of the piece
     * @return the new 2D representation of the board with this piece copied in.
     */
    public boolean[][] putInBoard(boolean[][] area, int x, int y){
        System.out.println("[Jugg] Putting " + part.name() + " in the board at position " + x + "," + y); //log

        //set the pieces in the map to True
        int xHeight = part.getNorthchunks();
        int yLength = part.getEastchunks();
        boolean[][] blocksFilled = part.getBlocksfilled();

        for(int xBlock = 0; xBlock < xHeight; xBlock++){
            for(int yBlock = 0; yBlock <yLength; yBlock++){
                if(blocksFilled[xBlock][yBlock]){
                    area[x+xBlock][y+yBlock] = true;
                }
            }
        }
        return area;
    }

    /**
     * Debug function to print the board. Currently unused.
     * @param area 2D array of the Board to print
     */
    public void printUpdatedArea(boolean[][] area){
        String board = "Board:\n";
        for(boolean[] x: area){
            for(boolean b: x){
                board += b ? "X":"-";
            }
            board+="\n";
        }
        System.out.println(board);
    }

    /**
     * Builds the piece in the given location
     * @param startx start of the X coordinate (most positive coord)
     * @param starty start of the Y coordinate (lowest point)
     * @param startz start of the Z coordinate (most negative coord)
     * @param curPosX current X position of the board
     * @param curPosY current Y position of the board
     */
    public void build(int startx, int starty, int startz, int curPosX, int curPosY) {
        System.out.println("[jugg] COPYING PIECE " + part.name() + " TO " + curPosX + ":" + curPosY + " at start " + startx + " : " + starty + " : " + startz); //log

        //calculate the world positions of x,y,z
        int x = startx - curPosX*MapPart.standardSize;
        int y = starty;
        int z = startz + curPosY*MapPart.standardSize;

        //copy all chunks of the piece to the new location
        int xHeight = part.getNorthchunks();
        int yLength = part.getEastchunks();
        boolean[][] blocksFilled = part.getBlocksfilled();

        for(int xBlock = 0; xBlock < xHeight; xBlock++){
            for(int yBlock = 0; yBlock<yLength; yBlock++){
                if(blocksFilled[xBlock][yBlock]){
                    copyChunk(xBlock, yBlock, x-(xBlock*MapPart.standardSize), y, z+(yBlock*MapPart.standardSize));
                }
            }
        }
    }

    /**
     * Copies a chunk to the new location
     * @param blockX current X of the map
     * @param blockZ current Y of the map
     * @param x start X position to copy to (most positive)
     * @param y start Y position to copy to (lowest)
     * @param z start Z position to copy to (most negative)
     */
    private void copyChunk(int blockX, int blockZ, int x, int y, int z){
        //find the start X and Z coords
        int startX = part.getStartX()-(MapPart.standardSize*blockX);
        int startZ = part.getStartZ()+(MapPart.standardSize*blockZ); //standard top left start

        //for every X,Y(til 128),Z, copy the next block
        for(int i = 0; i < MapPart.standardSize; i++){
            for(int j = 0; j < 128; j++){
                for(int k = 0; k<MapPart.standardSize; k++){
                    //Copy the old location block to the new location, including type data.
                    Location old = new Location(Bukkit.getWorlds().get(0), startX-i, y+j, startZ+k); //top-left corner
                    Location newLoc = new Location(Bukkit.getWorlds().get(0), x-i, y+j, z+k); //top-left corner
                    newLoc.getBlock().setType(old.getBlock().getType());
                    newLoc.getBlock().setData(old.getBlock().getData());

                    //if we find a sign, we need to copy the text on the sign too.
                    if(old.getBlock().getState() instanceof Sign){
                        Sign oldSign = (Sign) old.getBlock().getState();
                        Sign newSign = (Sign) newLoc.getBlock().getState();
                        for(int q = 0; q < 4; q++){
                            newSign.setLine(q, oldSign.getLine(q));
                        }
                        newSign.update();
                    }
                }
            }
        }
        return;
    }
}

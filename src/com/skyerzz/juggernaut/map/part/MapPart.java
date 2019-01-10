package com.skyerzz.juggernaut.map.part;

import java.util.ArrayList;
import java.util.Random;

/**
 * Lists all map parts and their format & location in the world.
 * Created by sky on 4-9-2018.
 */
public enum MapPart {
    //usage: ([POSX,NEGZ X COORD] [POSX,NEGZ Z COORD], [BLOCKCHUNKS IN X AXIS], [BLOCKSCHUNKS IN Z AXIS], [ARRAY AS HOW YOU READ, FACING EAST INGAME], PIECETYPE);
    //(Face east in-game, where posx negz is in the top left.

    //<editor-fold desc="[Bigsquare]">
    BIGSQUARE_ONE_SOUTH(1246, 169, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),
    BIGSQUARE_ONE_EAST(1313, -59, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),
    BIGSQUARE_ONE_NORTH(1085, -126, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),
    BIGSQUARE_ONE_WEST(1018, 102, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),

    BIGSQUARE_TWO_SOUTH(1246, 199, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),
    BIGSQUARE_TWO_EAST(1343, -59, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),
    BIGSQUARE_TWO_NORTH(1085, -156, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),
    BIGSQUARE_TWO_WEST(988, 102, 3, 3, new boolean[][]{{true, true, true}, { true, true, true}, {true, true, true}}, Type.PIECE),
    //</editor-fold>

    //<editor-fold desc="[Square]">
    SQUARE_ONE_SOUTH(1132, 160, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_ONE_EAST(1295, 55, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_ONE_NORTH(1190, -108, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_ONE_WEST(1027, -3, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),

    SQUARE_TWO_SOUTH(1132, 181, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_TWO_EAST(1316, 55, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_TWO_NORTH(1190, -129, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_TWO_WEST(1006, -3, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),

    SQUARE_THREE_SOUTH(1132, 202, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_THREE_EAST(1337, 55, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_THREE_NORTH(1190, -150, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_THREE_WEST(985, -3, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),

    SQUARE_FOUR_SOUTH(1132, 223, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_FOUR_EAST(1358, 55, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_FOUR_NORTH(1190, -171, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    SQUARE_FOUR_WEST(964, -3, 2, 2, new boolean[][]{{true, true}, {true, true}}, Type.PIECE),
    //</editor-fold>

    //<editor-fold desc="[Fat L]">
    FATL_ONE_SOUTH(1111, 160, 2, 2, new boolean[][]{{true, false}, {true, true}}, Type.PIECE),
    FATL_ONE_EAST(1295, 76, 2, 2, new boolean[][]{{false, true}, {true, true}}, Type.PIECE),
    FATL_ONE_NORTH(1211, -108, 2, 2, new boolean[][]{{true, true}, {false, true}}, Type.PIECE),
    FATL_ONE_WEST(1027, -24, 2, 2, new boolean[][]{{true, true}, {true, false}}, Type.PIECE),
    //</editor-fold>

    //<editor-fold desc="[Lengty]">
    LENGTY_ONE_SOUTH(1090, 160, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_ONE_EAST(1295, 97, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),
    LENGTY_ONE_NORTH(1223, -108, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_ONE_WEST(1027, -36, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),

    LENGTY_TWO_SOUTH(1090, 181, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_TWO_EAST(1316, 97, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),
    LENGTY_TWO_NORTH(1223, -129, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_TWO_WEST(1006, -36, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),

    LENGTY_THREE_SOUTH(1090, 202, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_THREE_EAST(1337, 97, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),
    LENGTY_THREE_NORTH(1223, -150, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_THREE_WEST(985, -36, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),

    LENGTY_FOUR_SOUTH(1090, 223, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_FOUR_EAST(1358, 97, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),
    LENGTY_FOUR_NORTH(1223, -171, 1, 2, new boolean[][]{{true, true}}, Type.PIECE),
    LENGTY_FOUR_WEST(964, -36, 2, 1, new boolean[][]{{true}, {true}}, Type.PIECE),
    //</editor-fold>

    //<editor-fold desc="[Filler]">

    FILLER_ONE_SOUTH(1078, 151, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_ONE_EAST(1277, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_ONE_NORTH(1235, -90, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_ONE_WEST(1036, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),

    FILLER_TWO_SOUTH(1078, 163, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_TWO_EAST(1289, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_TWO_NORTH(1235, -102, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_TWO_WEST(1024, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),

    FILLER_THREE_SOUTH(1078, 175, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_THREE_EAST(1301, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_THREE_NORTH(1235, -114, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_THREE_WEST(1012, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),

    FILLER_FOUR_SOUTH(1078, 187, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_FOUR_EAST(1313, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_FOUR_NORTH(1235, -126, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_FOUR_WEST(1000, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),

    FILLER_FIVE_SOUTH(1078, 199, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_FIVE_EAST(1325, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_FIVE_NORTH(1235, -138, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_FIVE_WEST(988, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),

    FILLER_SIX_SOUTH(1078, 211, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_SIX_EAST(1337, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_SIX_NORTH(1235, -150, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_SIX_WEST(976, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),

    FILLER_SEVEN_SOUTH(1078, 223, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_SEVEN_EAST(1349, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_SEVEN_NORTH(1235, -162, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_SEVEN_WEST(964, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),

    FILLER_EIGHT_SOUTH(1078, 235, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_EIGHT_EAST(1361, 109, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_EIGHT_NORTH(1235, -174, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    FILLER_EIGHT_WEST(952, -48, 1, 1, new boolean[][]{{true}}, Type.FILLER),
    //</editor-fold>

    //<editor-fold="[WALL]">
    WALL_ONE_SOUTH(1067, 151, 1,1, new boolean[][]{{true}}, Type.WALL);
    //</editor-fold>

    private final int startX;
    private final int startZ;
    private final int startY = 4;
    private final int northchunks;
    private final int eastchunks;
    /** 2d array represents the shape of the part */
    private final boolean[][] blocksfilled;
    private Type type;

    public final static int standardSize = 9;

    MapPart(int startX, int startZ, int northchunks, int eastchunks, boolean[][] blocksfilled, Type type){
        this.startX = startX;
        this.startZ = startZ;
        this.northchunks = northchunks;
        this.eastchunks = eastchunks;
        this.blocksfilled = blocksfilled;
        this.type = type;
    }

    /**
     * Gets a random map part from all options
     * @return MapPart at full random.
     */
    public static MapPart getRandomPart(){
        int i = MapPart.values().length;
        Random r = new Random();
        int part = r.nextInt(i);
        return MapPart.values()[part];
    }

    public boolean[][] getBlocksfilled() {
        return blocksfilled;
    }

    public int getNorthchunks() {
        return northchunks;
    }

    public int getEastchunks() {
        return eastchunks;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartZ() {
        return startZ;
    }

    public int getStartY() {
        return startY;
    }

    public Type getType(){ return type;}

    public enum Type{
        PIECE, //anywhere in the map
        WALL, //only on the outside of the map
        FILLER; //1 block structure used as a filler in the map
    }
}

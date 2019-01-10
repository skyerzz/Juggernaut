package com.skyerzz.juggernaut;

/**
 * Easy access particle colors
 * Created by sky on 5-9-2017.
 */
public enum ParticleColor {
    RED(0f, 0f, 0f),
    GREEN(-1f, 1f, 0f),
    LIGHT_BLUE(-0.5725f, 0.647f, 1f),
    BLUE(-1f, 0f, 1f),
    WHITE(0f, 1f, 1f),
    BLACK(-1f,0,0);

    private float red, green, blue;

    //note RED value is red value MINUS ONE. Minecraft just works that way.

    /**
     *
     * @param red float value for R(GB) value
     * @param green float value for (R)G(B) value
     * @param blue float value for (RG)B value
     */
    ParticleColor(float red, float green, float blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public float getRed(){ return red; }
    public float getGreen(){ return green; }
    public float getBlue(){ return blue; }
}

package com.skyerzz.juggernaut.game;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Spectator player for the Juggernaut Game
 * Created by sky on 23-11-2018.
 */
public class JuggernautSpectator extends JuggernautPlayer {

    public JuggernautSpectator(Player player){
        this.player = player;
        System.out.println("[Jugg] " +  player.getName() + " joined as a spectator!"); //log
    }

    /**
     * Activates the spectator mode
     */
    @Override
    public void activate(){
        //set the gamemode, and teleport them to the juggernaut.
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(JuggernautGame.getInstance().getJuggernaut().player);
        this.isAlive = false; //spectators are not alive by default.
    }

    /**
     * Destroys the spectator
     */
    @Override
    public void destroy(){
        //reset the gamemode, and put them back in the lobby.
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(JuggernautGame.lobbySpawn);
    }
}

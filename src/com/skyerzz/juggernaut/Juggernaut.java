package com.skyerzz.juggernaut;

import com.avaje.ebeaninternal.server.cluster.PacketTransactionEvent;
import com.skyerzz.juggernaut.command.JuggernautCommand;
import com.skyerzz.juggernaut.packetlistener.PacketInjector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main Juggernaut Class
 * Created by sky on 4-9-2018.
 */
public class Juggernaut extends JavaPlugin implements Listener {

    /**
     * Plugin Instance, used in the entire project to get Events registered
     */
    public static Juggernaut pluginInstance;

    public static final String version = "1.0.1";
    private static PacketInjector injector;

    /**
     * Enables the plugin
     */
    @Override
    public void onEnable() {
        getLogger().info("Booting up Juggernaut version " + version);
        pluginInstance = this;

        this.getCommand("juggernaut").setExecutor(new JuggernautCommand());

        getLogger().info("Juggernaut has been Enabled!");

        getLogger().info("Enabling packet listeners...");
        this.injector = new PacketInjector();

        this.getServer().getPluginManager().registerEvents(this, this);


    }

    /**
     * Disables the plugin
     */
    @Override
    public void onDisable(){
        getLogger().info("Shutting down...");
    }

    /**
     * When a player joins the server, we add them to our PacketInjector to listen for sent packets
     * @param event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        injector.addPlayer(event.getPlayer());
    }

    /*
        todo

        spec> armorstand of others to health
        create spectator mode properly
        chests get stuck on 0/X claiming after players die?
        (OPT) config file for map pieces //todo



        Juggernaut gets to 95% when hit when under 95% //done fixed
        remove health hotbar display when ded //Done
        lobby mechanism //done todo somewhat
        turn off armorstand heal me when > 2 blocks away //todo testing -- done
        proper death system //todo testing -- done
        end-of-game detection //todo testing -- done
        Heads with health of teammates //todo testing -- done
        initialise system before start game //done
        rotate pieces of map //done
        hint text above chest //Done
        heal notifications above hotbar (specially jugg) //DONE Note: jugg only.
        disable taking items from inventory //DONE
     */

}

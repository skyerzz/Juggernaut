package com.skyerzz.juggernaut.game.perk;

import com.skyerzz.juggernaut.Juggernaut;
import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * Abstract class for perks
 * Created by sky on 25-11-2018.
 */
public abstract class AbstractPerk implements Listener{

    /** name of the perk, usually overriden */
    protected String name = "Abstract Perk";
    /** current cooldown count */
    protected int cooldownCount = 0;

    protected boolean isCooldownRunning = false, shouldCancelCooldown = false;
    protected JuggernautPlayer player;

    /** itemslot of the perk */
    protected int slot;

    /** cooldown time */
    protected int COOLDOWN_START = 20;

    /**
     * Create this perk for the given player, in the given inventoryslot
     * @param player Player to instantiate for
     * @param slot slot in inventory perk is bound to
     */
    AbstractPerk(JuggernautPlayer player, int slot){
        this.player = player;
        this.slot = slot;
        Bukkit.getServer().getPluginManager().registerEvents(this, Juggernaut.pluginInstance);
    }

    /**
     * Gets the name of the perk
     * @return String name of the perk
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the wait item stack
     * @param amount Amount of items that should be in the stack
     * @return ItemStack (Bukkit) of the wait item
     */
    private ItemStack getWaitItem(int amount){
        return new ItemStack(Material.INK_SACK, amount, (byte)8);
    }

    /**
     * To be implemented by super classes
     * Should return the ItemStack of the actual perk item
     * @return
     */
    abstract ItemStack getPerkItem();

    /**
     * Initialises the perk. Called at the start of the game for all perks
     */
    abstract public void init();

    /**
     * activates the perk
     * Also called after cooldown
     */
    abstract public void activate();

    /**
     * Updates the perk, if needed
     */
    abstract public void update();

    /**
     * Removes the item from inventory
     */
    public void consume(){
        player.getPlayer().getInventory().setItem(slot, null);
    }

    /**
     * Disables the entire perk
     */
    public void disable(){
        HandlerList.unregisterAll(this);
        shouldCancelCooldown = true;
        consume();
    }

    /**
     * Gets the maximum cooldown
     * @return Cooldown when at max
     */
    public int getCooldown(){ return COOLDOWN_START;}

    /**
     * Runs the cooldown. When done, activates() the perk again.
     */
    protected void runCooldown(){
        if(COOLDOWN_START == -1){
            //this shouldnt happen? bad coding?
            System.out.println("[Jugg] Found -1 cooldown for " + this.getName() + ", ignoring cooldown run!");
            return;
        }
        if(isCooldownRunning){
            return; //if we are already running a cooldown, dont run it again!
        }
        isCooldownRunning = true; //turn on the cooldown
        cooldownCount = COOLDOWN_START; //reset the count to max
        new BukkitRunnable() { //run this every second (20 ticks)
            @Override
            public void run() {
                if(shouldCancelCooldown){ //if we should cancel the cooldown due to events that happened (e.g. game end), cancel neatly.
                    shouldCancelCooldown = false;
                    cooldownCount = 0;
                    this.cancel();
                    return;
                }
                if(--cooldownCount <= 0){ //subtract one of the cooldown and check if we're at the lowest cooldown yet. If we are, activate the perk and exit the cooldown.
                    isCooldownRunning = false;
                    shouldCancelCooldown = false;
                    this.cancel();
                    activate();
                    return;
                }
                player.getPlayer().getInventory().setItem(slot, getWaitItem(cooldownCount));
            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L, 20L);
    }

    @Override
    public String toString(){
        return "Perk: " + name;
    }
}

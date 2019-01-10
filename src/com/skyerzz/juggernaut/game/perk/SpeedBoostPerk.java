package com.skyerzz.juggernaut.game.perk;

import com.skyerzz.juggernaut.Juggernaut;
import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

/**
 * SpeedBoostPerk (SURVIVOR)
 * Gives the player a consumable Speedboost which increases speed by 50% for 3 seconds.
 * Has a cooldown of 30 seconds.
 * See base class AbstractPerk for javadocs of implemented functions
 * Created by sky on 29-11-2018.
 */
public class SpeedBoostPerk extends AbstractPerk {

    boolean isActive = false;
    private int secondsActive = 3; //amount of seconds the speedboost has

    SpeedBoostPerk(JuggernautPlayer player, int slot) {
        super(player, slot);
        this.name = "Speed Boost";
        this.COOLDOWN_START = 30;
    }

    @Override
    ItemStack getPerkItem() {
        ItemStack item = new ItemStack(Material.INK_SACK, 1, (byte)10);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Speed Boost");
        meta.setLore(Arrays.asList("\u00A79consumable", "\u00A77Cooldown: \u00A7c30s", "Increases speed by 50% for 2 seconds"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void init() {
        activate(); //instantly activate this perk
    }

    @Override
    public void activate() {
        player.getPlayer().getInventory().setItem(slot, getPerkItem());
        isActive = false; //its not currently active (its in cooldown)
    }

    @Override
    public void update() {

    }

    @Override
    public void consume(){
        super.consume();
    }

    /**
     * When clicked on this slot, run the speedboost
     * @param event
     */
    @EventHandler
    public void onInventoryClick(PlayerInteractEvent event){
        if(event.getPlayer().getUniqueId() == this.player.getPlayer().getUniqueId()){
            //its our player
            if(event.getPlayer().getInventory().getHeldItemSlot() == slot && !isActive){
                isActive = true; //set to active
                //its ours!
                System.out.println("[Jugg-SBP] Increased speed multiplier for player " + player.getPlayer().getName()); //log
                consume();
                player.setSpeedMultiplier(1.5d); //set the speed multiplier for the player to 1.5x
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setSpeedMultiplier(1.0d); //reset the speed multiplier
                        System.out.println("[Jugg-SBP] Reset speed multiplier for player " + player.getPlayer().getName());
                        runCooldown(); //start the cooldown
                    }
                }.runTaskLater(Juggernaut.pluginInstance, 20L * secondsActive); //wait secondsActive seconds before resetting the speed back to normal.
            }
        }
    }
}

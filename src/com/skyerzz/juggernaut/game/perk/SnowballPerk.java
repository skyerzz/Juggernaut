package com.skyerzz.juggernaut.game.perk;

import com.skyerzz.juggernaut.Juggernaut;
import com.skyerzz.juggernaut.game.JuggernautGame;
import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

/**
 * SnowballPerk (SURVIVOR)
 * Gives the player a snowball if the juggernaut has 100(+)% speed.
 * Has a cooldown of 20 seconds. Only replenishes if juggernaut is still at 100(+)% speed.
 * See base class AbstractPerk for javadocs of implemented functions
 * Created by sky on 27-11-2018.
 */
public class SnowballPerk extends AbstractPerk {

    private boolean isActive = false;

    SnowballPerk(JuggernautPlayer player, int slot) {
        super(player, slot);
        this.name = "Slow Ball";
        COOLDOWN_START = 20;
    }

    @Override
    ItemStack getPerkItem() {
        //return new ItemStack(Material.SNOW_BALL, 1);
        ItemStack item = new ItemStack(Material.SNOW_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Slow Ball");
        meta.setLore(Arrays.asList("\u00A79consumable", "\u00A77Cooldown: \u00A7c20s",  "Slows down the juggernaut by 5% flat upon hit"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void init() {
        //do nothing at start of the game due to jugg starting below 100%
    }

    @Override
    public void activate() {
        isActive = true; //set the perk to active and give them the snowball
        player.getPlayer().getInventory().setItem(slot, getPerkItem());
    }

    @Override
    public void update() {
        double speedPercentage = JuggernautGame.getInstance().getJuggernaut().getSpeedMultiplier(); //get the juggernaut speed
        if(speedPercentage>=0.999d){ //cant use 1d cause of doubles.
            //activate if not activated already!
            if(!isActive){
                activate();
            }
        }else{
            if(isActive){ //turn the perk back off, as juggernaut does not have 100%.
                isActive = false;
                shouldCancelCooldown = true;
                if(cooldownCount!=0) {
                    consume();
                }
            }
        }
    }

    /**
     * Called when a snowball is thrown, this will run the cooldown.
     * @param event
     */
    @EventHandler
    public void onSnowballThrow(ProjectileLaunchEvent event){
        if(event.getEntity().getShooter() instanceof Player){ //it was a player who shot out the entity.
            Player p = (Player) event.getEntity().getShooter();
            if(this.player.getPlayer().getUniqueId() == p.getUniqueId() && event.getEntity() instanceof Snowball){
                //its our player who threw a snowball
                cooldownCount = COOLDOWN_START;
                System.out.println("[Jugg] " + player.getPlayer().getName() + " shot their snowball! Restarting cooldown..."); //log message
                runCooldown(); //start the cooldown!
            }
        }
    }

}

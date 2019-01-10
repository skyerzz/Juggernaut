package com.skyerzz.juggernaut.game.perk;

import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

/**
 * EnderPearlPerk (SURVIVOR)
 * Gives the player a one-time ender pearl (without fall damage)
 * See base class AbstractPerk for javadocs of implemented functions
 * Created by sky on 25-11-2018.
 */
public class EnderPearlPerk extends AbstractPerk {


    EnderPearlPerk(JuggernautPlayer player, int slot) {
        super(player, slot);
        this.name = "Ender Pearl";
        COOLDOWN_START = -1; //one time perk
    }

    @Override
    ItemStack getPerkItem() {
        return new ItemStack(Material.ENDER_PEARL, 1);
    }

    @Override
    public void init() {
        activate();
    }

    @Override
    public void activate() {
        player.getPlayer().getInventory().setItem(slot, getPerkItem());
    }

    @Override
    public void update() {
        // this one does not have an update.
    }

    @Override
    public void disable() {
        super.disable();
    }

    /**
     * Disable the perk after its use has been completed.
     * @param event PlayerTeleporTevent
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        if(event.getPlayer().getUniqueId() == player.getPlayer().getUniqueId()){
            //its our player
            if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL){ // no damage for teleports!
                event.setCancelled(true);
                event.getPlayer().setNoDamageTicks(1);
                event.getPlayer().teleport(event.getTo());
                this.disable(); // we dont need this perk anymore!
            }
        }
    }
}

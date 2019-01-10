package com.skyerzz.juggernaut.game.supplychest;

import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Supply chest for helmets!
 * For Overrides see the base class javadocs.
 * Created by sky on 10-9-2018.
 */
public class HelmetSupplyChest extends AbstractSupplyChest {

    public HelmetSupplyChest(Location startLocation, byte direction) {
        super(1, startLocation, direction);
        this.displayName = "Helmet Supply Chest";
        //unique ID's for this chest. start at 9000.
        this.armorstandProgressbarID = 9000;
        this.armorstandItemID = 9001;
        this.armorstandClaimProgressID = 9002;
        this.armorstandResetChestID = 9003;
        this.armorstandInfoID = 9004;
        this.armorstandClaimInfoID = 9005;
    }

    @Override
    int getMaxLevel() {
        return 3;
    }

    @Override
    int getItemID(int level) {
        switch(level){
            case 0:
                return 298; //leather helm
            case 1:
                return 302; //chain helm
            case 2:
                return 306; //iron helm
            default: return 166; //barrier (error)
        }
    }

    @Override
    void applyItem(JuggernautPlayer player, int level) {
        switch(level){
            case 0:
                player.getPlayer().getInventory().setHelmet(makeUnbreakable(new ItemStack(Material.LEATHER_HELMET)));
                break;
            case 1:
                player.getPlayer().getInventory().setHelmet(makeUnbreakable(new ItemStack(Material.CHAINMAIL_HELMET)));
                break;
            case 2:
                player.getPlayer().getInventory().setHelmet(makeUnbreakable(new ItemStack(Material.IRON_HELMET)));
                break;
            default:
                System.out.println("Couldnt find level " + level + " for chest " + this.getClass().getSimpleName());
        }
    }
}

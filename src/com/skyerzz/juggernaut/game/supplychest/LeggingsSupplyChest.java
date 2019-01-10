package com.skyerzz.juggernaut.game.supplychest;

import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Supply chest for leggings!
 * For Overrides see the base class javadocs.
 * Created by sky on 10-9-2018.
 */
public class LeggingsSupplyChest extends AbstractSupplyChest {

    public LeggingsSupplyChest(Location startLocation, byte direction) {
        super(3, startLocation, direction);
        this.displayName = "Leggings Supply Chest";
        //unique ID's for this chest. start at 9020.
        this.armorstandProgressbarID = 9020;
        this.armorstandItemID = 9021;
        this.armorstandClaimProgressID = 9022;
        this.armorstandResetChestID = 9023;
        this.armorstandInfoID = 9024;
        this.armorstandClaimInfoID = 9025;
    }

    @Override
    int getMaxLevel() {
        return 3;
    }

    @Override
    int getItemID(int level) {
        switch(level){
            case 0:
                return 300; //leather legs
            case 1:
                return 304; //chain legs
            case 2:
                return 308; //iron legs
            default: return 166; //barrier (error)
        }
    }

    @Override
    void applyItem(JuggernautPlayer player, int level) {
        switch (level) {
            case 0:
                player.getPlayer().getInventory().setLeggings(makeUnbreakable(new ItemStack(Material.LEATHER_LEGGINGS)));
                break;
            case 1:
                player.getPlayer().getInventory().setLeggings(makeUnbreakable(new ItemStack(Material.CHAINMAIL_LEGGINGS)));
                break;
            case 2:
                player.getPlayer().getInventory().setLeggings(makeUnbreakable(new ItemStack(Material.IRON_LEGGINGS)));
                break;
            default:
                System.out.println("Couldnt find level " + level + " for chest " + this.getClass().getSimpleName());
        }
    }
}

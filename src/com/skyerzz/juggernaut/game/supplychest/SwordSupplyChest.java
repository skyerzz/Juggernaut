package com.skyerzz.juggernaut.game.supplychest;

import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Supply chest for swords!
 * For Overrides see the base class javadocs.
 * Created by sky on 10-9-2018.
 */
public class SwordSupplyChest  extends AbstractSupplyChest {

    public SwordSupplyChest(Location startLocation, byte direction) {
        super(0, startLocation, direction);
        this.displayName = "Sword Supply Chest";
        //unique ID's start at 8000
        this.armorstandProgressbarID = 8000;
        this.armorstandItemID = 8001;
        this.armorstandClaimProgressID = 8002;
        this.armorstandResetChestID = 8003;
        this.armorstandInfoID = 8004;
        this.armorstandClaimInfoID = 8005;
    }

    @Override
    int getMaxLevel() {
        return 3;
    }

    @Override
    int getItemID(int level) {
        switch(level){
            case 0:
                return 268; //wood sword
            case 1:
                return 272; //stone sword
            case 2:
                return 267; //iron sword
            default: return 166; //barrier (error)
        }
    }

    @Override
    void applyItem(JuggernautPlayer player, int level) {
        switch (level) {
            case 0:
                player.getPlayer().getInventory().setItem(0, makeUnbreakable(new ItemStack(Material.WOOD_SWORD)));
                break;
            case 1:
                player.getPlayer().getInventory().setItem(0, makeUnbreakable(new ItemStack(Material.STONE_SWORD)));
                break;
            case 2:
                player.getPlayer().getInventory().setItem(0, makeUnbreakable(new ItemStack(Material.IRON_SWORD)));
                break;
            default:
                System.out.println("[Jugg] ERROR: Couldnt find level " + level + " for chest " + this.getClass().getSimpleName());
        }
    }
}

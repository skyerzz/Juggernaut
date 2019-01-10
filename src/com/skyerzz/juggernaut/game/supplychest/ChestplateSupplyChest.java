package com.skyerzz.juggernaut.game.supplychest;

import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Supply chest for chestplates!
 * For Overrides see the base class javadocs.
 * Created by sky on 5-9-2018.
 */
public class ChestplateSupplyChest extends AbstractSupplyChest {

    public ChestplateSupplyChest(Location startLocation, byte direction) {
        super(2, startLocation, direction);
        this.displayName = "Chestplate Supply Chest";
        //unique ID's for this chest. start at 9010.
        this.armorstandProgressbarID = 9010;
        this.armorstandItemID = 9011;
        this.armorstandClaimProgressID = 9012;
        this.armorstandResetChestID = 9013;
        this.armorstandInfoID = 9014;
        this.armorstandClaimInfoID = 9015;
    }

    @Override
    int getMaxLevel() {
        return 3;
    }

    @Override
    int getItemID(int level) {
        switch(level){
            case 0:
                return 299; //leather chest
            case 1:
                return 303; //chain chest
            case 2:
                return 307; //iron chest
            default: return 166; //barrier (error)
        }
    }

    @Override
    void applyItem(JuggernautPlayer player, int level) {
        switch(level){
            case 0:
                player.getPlayer().getInventory().setChestplate(makeUnbreakable(new ItemStack(Material.LEATHER_CHESTPLATE)));
                break;
            case 1:
                player.getPlayer().getInventory().setChestplate(makeUnbreakable(new ItemStack(Material.CHAINMAIL_CHESTPLATE)));
                break;
            case 2:
                player.getPlayer().getInventory().setChestplate(makeUnbreakable(new ItemStack(Material.IRON_CHESTPLATE)));
                break;
            default:
                System.out.println("Couldnt find level " + level + " for chest " + this.getClass().getSimpleName());
        }
    }
}

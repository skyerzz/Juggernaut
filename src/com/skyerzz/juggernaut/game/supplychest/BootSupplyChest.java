package com.skyerzz.juggernaut.game.supplychest;

import com.skyerzz.juggernaut.game.JuggernautPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Supply chest for boots!
 * For Overrides see the base class javadocs.
 * Created by sky on 10-9-2018.
 */
public class BootSupplyChest extends AbstractSupplyChest {

    public BootSupplyChest(Location startLocation, byte direction) {
        super(4, startLocation, direction);
        this.displayName = "Boot Supply Chest";
        //unique ID's for this chest. start at 9030.
        this.armorstandProgressbarID = 9030;
        this.armorstandItemID = 9031;
        this.armorstandClaimProgressID = 9032;
        this.armorstandResetChestID = 9033;
        this.armorstandInfoID = 9034;
        this.armorstandClaimInfoID = 9035;
    }
    @Override
    int getMaxLevel() {
        return 3;
    }

    @Override
    int getItemID(int level) {
        switch(level){
            case 0:
                return 301; //leather boots
            case 1:
                return 305; //chain boots
            case 2:
                return 309; //iron boots
            default: return 166; //barrier (error)
        }
    }

    @Override
    void applyItem(JuggernautPlayer player, int level) {
        switch (level) {
            case 0:
                player.getPlayer().getInventory().setBoots(makeUnbreakable(new ItemStack(Material.LEATHER_BOOTS)));
                break;
            case 1:
                player.getPlayer().getInventory().setBoots(makeUnbreakable(new ItemStack(Material.CHAINMAIL_BOOTS)));
                break;
            case 2:
                player.getPlayer().getInventory().setBoots(makeUnbreakable(new ItemStack(Material.IRON_BOOTS)));
                break;
            default:
                System.out.println("Couldnt find level " + level + " for chest " + this.getClass().getSimpleName());
        }
    }
}

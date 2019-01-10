package com.skyerzz.juggernaut;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * ItemHelper class to create certain items.
 * Created by sky on 17-11-2018.
 */
public class SkyItemHelper {

    /**
     * Creates a stack of player skulls of the given skullowner
     * @param amount amount in the stack
     * @param skullowner Player owner of the skull
     * @return
     */
    public static ItemStack getSkullStack(int amount, Player skullowner){
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, amount, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(skullowner.getName());
        meta.setDisplayName(skullowner.getName() + "'s Health");
        skull.setItemMeta(meta);
        return skull;
    }
}

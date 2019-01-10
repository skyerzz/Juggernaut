package com.skyerzz.juggernaut.game.perk.mobs;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.util.Map;

import static com.skyerzz.juggernaut.game.perk.mobs.WatcherZombie.getPrivateField;

/**
 * Class for all custom mob entities in the juggernaut game
 * Created by sky on 14-12-2018.
 */
public enum MobEntities {

    WatcherZombie("WatcherZomb", 54, WatcherZombie.class);

    private int id;
    private String name;
    private Class mob;

    /**
     * Registers a new custom mob
     * @param name Name of the mobEntity
     * @param id ID given to the entity
     * @param mob Class of the entity
     */
    MobEntities(String name, int id, Class<? extends Entity> mob){
        //System.out.println("[JUGG] Found custom mob: " + name);
        this.id = id;
        this.name = name;
        this.mob = mob;
        addToMaps(mob, name, id);
    }

    /**
     * Spawns the entity given
     * @param entity EntityClass to spawn
     * @param loc Location to spawn him at
     */
    public static void spawnEntity(Entity entity, Location loc)
    {
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld)loc.getWorld()).getHandle().addEntity(entity);
    }

    /**
     * Adds a custom mob to the spigot directory
     * @param clazz Class of the mob
     * @param name Name of the mob
     * @param id ID of the mob
     */
    private static void addToMaps(Class clazz, String name, int id){ //from https://www.spigotmc.org/threads/tutorial-creating-custom-entities-with-pathfindergoals.18519/
        //System.out.println("[Jugg] Registering custom mob " + name);
        ((Map)getPrivateField("c", EntityTypes.class, null)).put(name, clazz);
        ((Map)getPrivateField("d", EntityTypes.class, null)).put(clazz, name);
        ((Map)getPrivateField("f", EntityTypes.class, null)).put(clazz, Integer.valueOf(id));
    }
}

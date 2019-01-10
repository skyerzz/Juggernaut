package com.skyerzz.juggernaut.game.perk.mobs;

import com.skyerzz.juggernaut.game.JuggernautGame;
import com.skyerzz.juggernaut.game.JuggernautPlayer;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.PathfinderGoalTarget;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.ArrayList;

/**
 * Pathfinder which selects the closest survivor within a radius in a juggernaut game
 * Created by sky on 14-12-2018.
 */
public class TargetSelectorJuggernautSurvivor  extends PathfinderGoalTarget {

    /** maximum radius from base */
    private double maxRadius;
    /** currently selected target */
    private JuggernautPlayer target;

    /**
     * Instances a new pathfindergoal
     * @param entityCreature Entity to receive the pathfindergoal
     * @param b Not entirely sure what this does. Mostly set to True, seems to work.
     * @param maxRadius Maximum radius to search for survivors.
     */
    public TargetSelectorJuggernautSurvivor(EntityCreature entityCreature, boolean b, double maxRadius) {
        super(entityCreature, b);
        this.maxRadius = maxRadius;
    }

    /**
     * Called every tick, sets the closest player
     * @return True if it has a target, false otherwise.
     */
    @Override
    public boolean a() {
        JuggernautPlayer closest = null; //find the closest player in line of sight
        double distance = Integer.MAX_VALUE;
        for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
            if(!JP.isSurvivor() || !JP.isAlive()){ //dont target the juggernaut or dead players
                continue;
            }
            boolean hasLineOfSight = ((CraftPlayer) JP.getPlayer()).getHandle().hasLineOfSight(this.e);
            if(!hasLineOfSight){ //if the player is in radius, but we have no line of sight, skip him.
                continue;
            }
            double d = JP.getPlayer().getLocation().distance(getExactLocation(this.e));
            if(d<=maxRadius){
                if(distance > d){ //if we found a closer player than we have untill now, set him
                    distance = d;
                    closest = JP;
                }
            }
        }

        if(closest!=null){ //if we found a player, set them as a target and indicate we need to do something
            this.target = closest;
            return true;
        }

        return false; //we didnt have a target, so we aint doing anything

    }

    /**
     * Returns the exact Bukkit location of a NMS entity
     * @param creature Entity to locate
     * @return Bukkit Location of the entity
     */
    private Location getExactLocation(Entity creature){
        return new Location(creature.getWorld().getWorld(), creature.locX, (int)creature.locY, creature.locZ, 0f, 0f);
    }

    /**
     * Called every update when a() returns true
     * Updates the goal target if needed
     */
    @Override
    public void e(){
        if(target==null && this.e.getGoalTarget()!=null){ //if our target is null, set it to null for the entity
            this.e.setGoalTarget(null);
            return;
        }
        this.e.setGoalTarget(((CraftPlayer) target.getPlayer()).getHandle(), EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true); //set the entities' target
    }
}

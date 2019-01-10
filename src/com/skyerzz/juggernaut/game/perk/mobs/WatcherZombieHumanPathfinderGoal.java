package com.skyerzz.juggernaut.game.perk.mobs;

import com.skyerzz.juggernaut.SkyMathHelper;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Pathfinder goal towards the nearest survivor, or base.
 * Created by sky on 14-12-2018.
 */
public class WatcherZombieHumanPathfinderGoal extends PathfinderGoal {

    private EntityCreature creature;
    private double speed;
    private Location base;
    private double maxRadius;
    private int status = 0;

    private static final int IDLE = 0, RETURNING = 1, CHARGING = 2;

    public WatcherZombieHumanPathfinderGoal(EntityCreature creature, double speed, Location base, double maxRadius){
        this.creature = creature;
        this.speed = speed;
        this.base = base;
        base.setX(base.getBlockX()+0.5d);
        base.setY(base.getBlockY());
        base.setZ(base.getBlockZ()+0.5d);
        base.setYaw(0f);
        base.setPitch(0f); //sets the base position.
        this.maxRadius = maxRadius;
    }

    /**
     * gets called every tick, updates the goal/Status if needed.
     * @return True if updated, False if not
     */
    @Override
    public boolean a() {
        //System.out.println("[JUGGY] a called");
        updateLineOfSight(creature);
        EntityLiving alive = creature.getGoalTarget();
        if(alive==null){
            //if we're not on base position, go there!
            if(!ishome()) {
                //System.out.println("RETURNING");
                status = RETURNING;
                return true;
            }
            //System.out.println("Idle");
            status = IDLE;
            return false;
        }
        //System.out.println("Charge?");
        //CHARGE?
        if(!SkyMathHelper.isInRadius(base, getExactLocation(creature), maxRadius, false) && !SkyMathHelper.isInRadius(base, getExactLocation(alive), maxRadius, false)){
            //no charge :c
            status = IDLE;
            return false;
        }
        //System.out.println("CHARGING!");
        //we're on it, lets charge!
        //They have line of sight, and are in range of the base, so lets follow them around a bit!
        status = CHARGING;
        return true;
    }

    /**
     * Gets bukkit Middle-of-block location of a NMS entity
     * @param creature Entity to get the location from
     * @return Bukkit Middle-of-block (X.5,X.5) location
     */
    private Location getLocation(Entity creature){
        return new Location(creature.getWorld().getWorld(), ((int) creature.locX)+0.5d, (int)creature.locY, ((int) creature.locZ)+0.5d, 0f, 0f);
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
     * Checks if the entity is at its base location
     * @return Boolean status
     */
    private boolean ishome(){
        return getLocation(creature).equals(base);
    }

    /**
     * Updates the line of sight and goaltarget of the creature
     * @param creature creature to update
     */
    private void updateLineOfSight(EntityCreature creature){
        EntityLiving target = creature.getGoalTarget();
        if(target==null){ //doesnt have a target, so we dont have much to update
            return;
        }
        boolean sight = creature.hasLineOfSight(target); //check if they actually have a line of sight towards the target
        if(getLocation(creature).distance(getLocation(target)) > maxRadius || !sight){ //if they dont have line of sight, or its outside the radius, reset the goal target.
            creature.setGoalTarget(null);
        }
    }

    /**
     * Called every time a() tells us to.
     * Updates the navigation of the creature, if needed.
     */
    @Override
    public void e(){
        //System.out.println("[JUGGY] c called");
        if(status==IDLE){ // when idle he doesnt call this function anyways...
            //System.out.println("[JUGGY] c called IDLE");
            return;
        }

        if(status== RETURNING) { //if he's returning to base, make him go there!
            //System.out.println("[JUGGY] c called RETURNING");
            PathEntity path = creature.getNavigation().a(base.getX(), base.getY(), base.getZ());
            creature.getNavigation().a(path, speed);
            return;
        }

        if(status==CHARGING){ //he's charging towards his goal target, lets help him out by setting his navigation.
            //System.out.println("[JUGGY] c called CHARGING");
            EntityLiving alive = creature.getGoalTarget();
            Location aLoc = getExactLocation(alive);

            Vector dir = aLoc.toVector().subtract(getExactLocation(creature).toVector());
            //System.out.println("dir " + dir.getX() + ": " + dir.getZ());
            Location chargeLoc = getExactLocation(creature).add(dir.normalize().multiply(1.5d));
            if(base.distance(chargeLoc) < maxRadius){
                //System.out.println("Going to " + chargeLoc.getX() + ":" + chargeLoc.getY() + ":" + chargeLoc.getZ());
                PathEntity path = creature.getNavigation().a(chargeLoc.getX(), chargeLoc.getY(), chargeLoc.getZ());
                creature.getNavigation().a(path, speed);
            }


            //attack animation & functionality ripped from base zombie class.
            if(getExactLocation(creature).distance(getExactLocation(alive)) < 1){
                if(this.creature.bA() != null) {
                    this.creature.bw(); //this is attack animation
                }

                this.creature.r(alive); //this is attack the target
            }
            return;
        }
    }
}

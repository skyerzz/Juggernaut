package com.skyerzz.juggernaut.game.perk.mobs;

import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.Material;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by sky on 13-12-2018.
 */
public class WatcherZombie extends EntityZombie {

    private Location base;

    public WatcherZombie(Location loc) {
        super(((CraftWorld) loc.getWorld()).getHandle());
        base = loc;

        List goalB = (List)getPrivateField("b", PathfinderGoalSelector.class, goalSelector); goalB.clear();
        List goalC = (List)getPrivateField("c", PathfinderGoalSelector.class, goalSelector); goalC.clear();
        List targetB = (List)getPrivateField("b", PathfinderGoalSelector.class, targetSelector); targetB.clear();
        List targetC = (List)getPrivateField("c", PathfinderGoalSelector.class, targetSelector); targetC.clear();


        this.goalSelector.a(2, new WatcherZombieHumanPathfinderGoal(this, 1.0D, base, 10));


        this.goalSelector.a(0, new PathfinderGoalFloat(this));
//        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
//        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
//        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
//        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
//        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));

//        this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, EntityIronGolem.class, 1.0D, true));
//        this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
//        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[]{EntityPigZombie.class}));
//        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.targetSelector.a(2, new TargetSelectorJuggernautSurvivor(this, true, 10));

    }

    public void damage(double damage){
        //System.out.println("Damage: " + damage  + " ::>> " + this.getHealth());
        this.setHealth((float) (this.getHealth()-damage));
    }

    public void setSword(int materialID){
        //item slots: 0=sword, 1=boots, 3=legplate, 2=chestplate, ?=helmet
        this.setEquipment(0, new ItemStack(Item.getById(materialID)));
    }

    public void setHelmet(int materialID){
        //item slots: 0=sword, 1=boots, 3=legplate, 2=chestplate, ?=helmet
        this.setEquipment(4, new ItemStack(Item.getById(materialID)));
    }


    public static Object getPrivateField(String fieldName, Class clazz, Object object){
        Field field;
        Object o = null;
        try{
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch(NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }
        return o;
    }
}

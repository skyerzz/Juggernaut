package com.skyerzz.juggernaut.game.perk;

import com.skyerzz.juggernaut.game.JuggernautPlayer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sky on 27-11-2018.
 */
public class PerkHandler {

    private static HashMap<Class<? extends AbstractPerk>, ArrayList<JuggernautPlayer>> subscribers = new HashMap<>();

    static{
        subscribers.put(EnderPearlPerk.class, new ArrayList<>());
        subscribers.put(SpeedBoostPerk.class, new ArrayList<>());
        subscribers.put(SnowballPerk.class, new ArrayList<>());


        //juggy perks
        subscribers.put(ZombieWatcherPerk.class, new ArrayList<>());
    }

    public static void subscribe(JuggernautPlayer player, Class<? extends AbstractPerk> perk, int slot){
        if(subscribers.containsKey(perk)){
            if(!subscribers.get(perk).contains(player)){ // dont double register
                subscribers.get(perk).add(player);
                try {
                    player.addPerk(perk.getDeclaredConstructor(JuggernautPlayer.class, int.class).newInstance(player, slot));
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }else{
            System.out.println("[Jugg] Perk error [SUB]: Cannot find perk " + perk.getName());
        }
    }

    public static void unsubscribe(JuggernautPlayer player, Class<? extends AbstractPerk> perk){
        if(subscribers.containsKey(perk)){
            subscribers.get(perk).remove(player);
        }else{
            System.out.println("[Jugg] Perk error [UNSUB]: Cannot find perk " + perk.getName());
        }

    }

    public static void updateSubscribers(Class<? extends AbstractPerk> perk){
        if(subscribers.containsKey(perk)){
            for(JuggernautPlayer player: subscribers.get(perk)){
                player.getPerkByClass(perk).update();
            }
        }else{
            System.out.println("[Jugg] Perk error [UPDATE]: Cannot find perk " + perk.getName());
        }
    }

    public static void initialisePerk(Class<? extends AbstractPerk> perk){
        if(subscribers.containsKey(perk)){
            for(JuggernautPlayer player: subscribers.get(perk)){
                if(player.getPerkByClass(perk)!=null){
                    player.getPerkByClass(perk).init();
                }
            }
        }else{
            System.out.println("[Jugg] Perk error [ACTIVATE]: Cannot find perk " + perk.getName());
        }

    }

    public static void initialiseAll(){
        for(Class<? extends AbstractPerk> clazz: subscribers.keySet()){
            initialisePerk(clazz);
        }
    }
}

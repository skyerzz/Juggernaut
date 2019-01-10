package com.skyerzz.juggernaut.game.perk;

import com.skyerzz.juggernaut.game.JuggernautGame;
import com.skyerzz.juggernaut.game.JuggernautPlayer;
import com.skyerzz.juggernaut.game.perk.mobs.MobEntities;
import com.skyerzz.juggernaut.game.perk.mobs.WatcherZombie;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * ZombieWatcherPerk (JUGGERNAUT)
 * Spawns a zombieWatcher in your location
 * Has a cooldown of 60 seconds. Cooldown starts when old zombiewatcher has died.
 * See base class AbstractPerk for javadocs of implemented functions
 * Created by sky on 15-12-2018.
 */
public class ZombieWatcherPerk extends AbstractPerk {

    private WatcherZombie zomb;
    private Material sword;
    private Material helm, chest, legs, boots;
    private boolean isActive = false;

    ZombieWatcherPerk(JuggernautPlayer player, int slot) {
        super(player, slot);
        sword = Material.IRON_SWORD;
        helm = Material.IRON_HELMET;
        name = "Zombie Watcher";
        COOLDOWN_START = 60;
    }

    @Override
    ItemStack getPerkItem() {
        return new ItemStack(Material.MONSTER_EGG);
    }

    @Override
    public void init() {
        activate();
    }

    @Override
    public void activate() {
        //System.out.println("Activating Zombie WAtcher...");
        player.getPlayer().getInventory().setItem(slot, getPerkItem());
        isActive = false; //ready to use!
    }

    public void runZombie(){
        System.out.println("[Jugg:ZombieWatcher] The zombie is loose!"); //log
        isActive = true;
        consume();
        this.zomb = new WatcherZombie(player.getPlayer().getLocation()); //create a new WatcherZombie at the location of activation

        //set the zombie's items.
        zomb.setSword(helm.getId());
        zomb.setHelmet(sword.getId());

        //actually spawn the zombie
        MobEntities.spawnEntity(zomb, player.getPlayer().getLocation());

    }

    @Override
    public void update() {

    }

    /**
     * Calculates the attack value of the watcherzombie
     * @return double attackvalue
     */
    private double calculateAttack(){
        double baseDamage = sword==Material.DIAMOND_SWORD ? 7 : sword==Material.IRON_SWORD ? 6 : sword==Material.STONE_SWORD ? 5 : sword==Material.WOOD_SWORD ? 4 : 2;
        return baseDamage;
    }

    /**
     * Calculates the damage the attacker deals to the player
     * @param attacker This class (our zombie)
     * @param player Player to be attacked
     * @return double final damage
     */
    private double calculateDamage(EntityLiving attacker, JuggernautPlayer player){
        double finalDamage = calculateAttack() * (1d-player.calculateArmorValues(player));
        return finalDamage;
    }

    /**
     * Calculates damage done against our zombie
     * @param attacker Player who attackes
     * @param def This class (our zombie)
     * @return double final damage dealt
     */
    private double calculateDamage(JuggernautPlayer attacker, EntityLiving def){
        double finalDamage = attacker.calculateAttack(attacker) * (1d-calculateArmorValues());
        return finalDamage;
    }

    /**
     * Calculates the armor resistance value of our zombie
     * @return double resistance value
     */
    private double calculateArmorValues(){
        double armor = 0d;
        armor += helm==Material.LEATHER_HELMET? 0.07d : helm==Material.CHAINMAIL_HELMET ? 0.1d : helm==Material.IRON_HELMET ? 0.13d : helm==Material.DIAMOND_HELMET ? 0.17d : 0;
        armor += chest==Material.LEATHER_CHESTPLATE? 0.07d : chest==Material.CHAINMAIL_CHESTPLATE ? 0.1d : chest==Material.IRON_CHESTPLATE ? 0.13d : chest==Material.DIAMOND_CHESTPLATE ? 0.17d : 0;
        armor += legs==Material.LEATHER_LEGGINGS? 0.07d : legs==Material.CHAINMAIL_LEGGINGS ? 0.1d : legs==Material.IRON_LEGGINGS ? 0.13d : legs==Material.DIAMOND_LEGGINGS ? 0.17d : 0;
        armor += boots==Material.LEATHER_BOOTS? 0.07d : boots==Material.CHAINMAIL_BOOTS ? 0.1d : boots==Material.IRON_BOOTS ? 0.13d : boots==Material.DIAMOND_BOOTS ? 0.17d : 0;
        
        return armor;
    }

    /**
     * When our zombie dies, run the cooldown
     * @param event EntityDeathEvent
     */
    @EventHandler
    public void onZombieDeath(EntityDeathEvent event){
        if(event.getEntity().getUniqueId() == zomb.getUniqueID()){
            //he ded.
            System.out.println("[Jugg] R.I.P. zombieWatcher. Starting cooldown!"); //log
            runCooldown();
        }
    }

    /**
     * Called when the zombie takes or gives damage
     * @param e EntityDamageByEntityEvent
     */
    @EventHandler
    public void onZombieDamage(EntityDamageByEntityEvent e){
        if(e.getDamager().getUniqueId() == zomb.getUniqueID()){
            //its our zombie
            //System.out.println("Our zombie damaged");
            e.setDamage(0);//dont have default damage. default damage sucks!

            //get the player who was damaged by our zombiewatcher
            JuggernautPlayer JP = JuggernautGame.getInstance().getJuggernautPlayer(e.getEntity().getUniqueId());
            if(JP==null || !JP.isAlive()){
                return; //how did this happen?
            }

            //calculate damage dealt, and apply.
            double damageToDeal = calculateDamage(zomb, JP);
            JP.damagePlayer(damageToDeal);

        }else if(e.getEntity().getUniqueId() == zomb.getUniqueID()){
            //our zombie got damaged!
            e.setDamage(0); //default damage still sucks.
            System.out.println("Our zombie took damage"); //log

            //get the player who damaged our zombie
            JuggernautPlayer JP = JuggernautGame.getInstance().getJuggernautPlayer(e.getDamager().getUniqueId());
            if(JP==null || !JP.isAlive()){
                return; //how did this happen?
            }

            //calculate damage dealt to us, and apply.
            double damageToDeal = calculateDamage(JP, zomb);
            zomb.damage(damageToDeal);
        }
    }

    /**
     * Called when the player activates the perk, spawns the zombie (if not active)
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void onSpawn(PlayerInteractEvent event){
        if(event.getPlayer().getUniqueId() == this.player.getPlayer().getUniqueId()){
            //its our juggy <3
            if(this.player.getPlayer().getInventory().getHeldItemSlot() == this.slot){
                //they clicked our slot!

                event.setCancelled(true); //in case of spawn egg, dont actually run it
                if(isActive){
                    return; //already active
                }

                //Zombie gets spawn.
                runZombie();
            }
        }
    }
}

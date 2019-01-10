package com.skyerzz.juggernaut.game;

import com.skyerzz.juggernaut.Juggernaut;
import com.skyerzz.juggernaut.PacketHelper;
import com.skyerzz.juggernaut.SkyItemHelper;
import com.skyerzz.juggernaut.SkyMathHelper;
import com.skyerzz.juggernaut.game.perk.*;
import com.skyerzz.juggernaut.game.supplychest.AbstractSupplyChest;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by sky on 5-9-2018.
 */
public class JuggernautPlayer implements Listener {

    protected Player player;
    private HashMap<Integer,  ArrayList<Entity>> loadedEntities = new HashMap<>();
    private HashMap<Integer, Integer> claimedChestLevels = new HashMap<>();
    private HashMap<UUID, Entity> hiddenNames = new HashMap<>();
    private boolean survivor = true;
    private boolean killSpeedLoop = false, killSprintLoop =false, killNameLoop = false;
    private final float defaultSpeed = 0.28f;
    private double minSpeed = 0.85d, maxSpeed = 1.15d, currentMultiplierSpeed = 1.0d, minHitSpeed = 0.85d;
    private boolean killFunctionTimer = false;
    private static final String NAMETAG_HURT = "\u00A7cDamaged! \u00a7eSneak to heal!";
    private static final double maxDamageDisplayRadius = 5d;
    private boolean isDamagedLoopRunning = false, isTextDamageLoopRunning = false, hasSnowball = false;
    private long lastSnowBallThrown = 0;
    private double currentSpeedMultiplier = 1; //all speeds are multiplied with this.

    boolean isAlive = true;

    private ArrayList<AbstractPerk> perks = new ArrayList<>();

    public void addPerk(AbstractPerk perk){
        perks.add(perk);
        System.out.println(this.getPlayer().getName() + " Added perk " + perk.toString());
    }

    //timer for speed increase
    int loopTimer = 0;

    public JuggernautPlayer(Player player){
        this.player = player;
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.teleport(JuggernautGame.lobbySpawn);
        player.setGameMode(GameMode.ADVENTURE);
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(4);
        player.setSaturation(0f);
        //System.out.println("[Jugg] " + player.getName() + " joined the game!");
    }

    public JuggernautPlayer() {
    }

    public Player getPlayer(){
        return player;
    }

    public void addLoadedNameEntry(Player p, Entity e){
        //System.out.println("Adding name entry for player " + this.getPlayer().getName() + " :: " + p.getName() + " - ECN:" + e.getCustomName());
        hiddenNames.put(p.getUniqueId(), e);
    }

    public void removeNameEntry(Player p){
        Entity e = hiddenNames.remove(p.getUniqueId());
        if(e!=null){
            PacketHelper.despawnEntity(this.getPlayer(), e);
        }
    }

    public HashMap<Integer, ArrayList<Entity>> getLoadedEntities(){
        return loadedEntities;
    }

    public void addLoadedEntities(int customID, Entity...entities){
        loadedEntities.put(customID, new ArrayList<>(Arrays.asList(entities)));
    }

    public void displayHologram(double x, double y, double z, String text, int customID){
        EntityArmorStand stand = PacketHelper.displayHologram(player, x, y, z, text);
        addLoadedEntities(customID, stand);
    }

    public void updateHologram(int customID, String updatedName){
        ArrayList<Entity> stand = getLoadedEntities().get(customID);
        if(stand==null){
            return;
        }
        PacketHelper.updateHologram(player, (EntityArmorStand) stand.get(0), updatedName);
    }

    public void removeEntities(int...customID){
        for(int ID: customID) {
            ArrayList<Entity> stand = getLoadedEntities().remove(ID);
            if (stand != null) {
                //System.out.println("Removing stand " + ID);
                for (Entity e : stand) {
                    PacketHelper.despawnEntity(player, e);
                }
            }
        }
    }

    public void updateChestState(Location location, boolean open){
        PacketHelper.setChestStatus(player, location, open);
    }

    public void displayFloatingItem(double x, double y, double z, net.minecraft.server.v1_8_R3.ItemStack item, int customID){
        ArrayList<Entity> entities = PacketHelper.displaySuspendedRotatingItem(player, x, y, z, item);
        loadedEntities.put(customID, entities);
    }

    public int getChestBonus(int chestID){
        if(claimedChestLevels.containsKey(chestID)){
            return claimedChestLevels.get(chestID);
        }else{
            return -1;
        }
    }

    public void setChestBonus(int chestID, int level){
        System.out.println("[Jugg] " + player.getName() + " claimed " + chestID + " , Level " + level);
        claimedChestLevels.put(chestID, level);
    }

    public boolean isSurvivor(){
        return survivor;}

    public void setSurvivor(boolean survivor) {
        this.survivor = survivor;
        if(!isSurvivor()){
            indexJuggernaut();
            runSpeedLoop();
        }else{
            killSpeedLoop = true;
        }
    }

    //footsteps
    private void runParticleSprintLoop(){
        new BukkitRunnable(){

            @Override
            public void run() {
                if(killSprintLoop){
                    killSprintLoop = false;
                    this.cancel();
                    return;
                }
                if(!player.isSneaking()){
                    Location loc = player.getLocation();
                    loc.setY(loc.getY()+0.02d);
                    //PacketHelper.showParticle(JuggernautGame.getInstance().getJuggernaut().getPlayer(), loc, EnumParticle.FOOTSTEP, 5);
                }
            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L, 4L);
    }

    private void runSpeedLoop(){
        new BukkitRunnable(){

            int loopsTillSpeedDecrease = 150;
            float increaseSpeed = 0.01f;

            @Override
            public void run() {
                if(killSpeedLoop){
                    this.cancel();
                    return;
                }
                if(loopTimer++>=loopsTillSpeedDecrease){
                    loopTimer = 0;
                    currentMultiplierSpeed = Math.max(minSpeed, Math.min(maxSpeed, (currentMultiplierSpeed+ increaseSpeed)));
                }
                player.setExp(currentMultiplierSpeed==maxSpeed? 0 : ((float) loopTimer/(float)loopsTillSpeedDecrease));
                updateSpeed();
            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L, 2L); //every 1/10th second (10 times per second)
    }

    private void updateSpeed(){
        setSpeed((float) (currentMultiplierSpeed*defaultSpeed * currentSpeedMultiplier));
        //lets update our perks that depend on juggy speed
        if(!this.isSurvivor()) {
            player.setLevel((int) Math.ceil((currentMultiplierSpeed * 100)));
            PerkHandler.updateSubscribers(SnowballPerk.class);
        }
    }

    private void changeSpeedMultiplier(double change){
        currentMultiplierSpeed += change;
        if(currentMultiplierSpeed < minSpeed){
            currentMultiplierSpeed = minSpeed;
        }else if(currentMultiplierSpeed > maxSpeed){
            currentMultiplierSpeed = maxSpeed;
        }
        updateSpeed();
    }

    private void indexJuggernaut(){
        currentMultiplierSpeed = 0.85d;
        setSpeed((float) (defaultSpeed * currentMultiplierSpeed));
        player.setMaxHealth(30d);
        player.setHealth(30d);

        ItemStack diaHelmet = new ItemStack(Material.DIAMOND_HELMET, 1);
//        ItemMeta meta = diaHelmet.getItemMeta();
//        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
//        diaHelmet.setItemMeta(meta);
        player.getInventory().setHelmet(diaHelmet);
        ItemStack diaChest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
//        meta = diaChest.getItemMeta();
//        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
//        diaChest.setItemMeta(meta);
        player.getInventory().setChestplate(diaChest);
        ItemStack diaLegg = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
//        meta = diaLegg.getItemMeta();
//        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
//        diaLegg.setItemMeta(meta);
        player.getInventory().setLeggings(diaLegg);
        ItemStack diaBoots = new ItemStack(Material.DIAMOND_BOOTS, 1);
//        meta = diaBoots.getItemMeta();
//        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
//        diaBoots.setItemMeta(meta);
        player.getInventory().setBoots(diaBoots);

        ItemStack diaSword = new ItemStack(Material.DIAMOND_SWORD, 1);
        player.getInventory().addItem(diaSword);
    }

    public void setSpeed(float speed){
        //System.out.println("Setting speed to " + speed);
        player.setWalkSpeed(speed);
    }

    public void activate(){
        Bukkit.getServer().getPluginManager().registerEvents(this, Juggernaut.pluginInstance);
        player.setGameMode(GameMode.ADVENTURE);
        killFunctionTimer = false;
        functionTimer();
        killSprintLoop = false;
        runParticleSprintLoop();
        System.out.println("Setting food level for player " + player.getName() + " to 4");
        player.setFoodLevel(4);
        player.setMaxHealth(20);
        player.setHealth(20);
        setSpeed(defaultSpeed);

        hideNameTag();
        updateHealthFromPlayers();

        if(this.isSurvivor()){
            PerkHandler.subscribe(this, SpeedBoostPerk.class, 1);
            PerkHandler.subscribe(this, SnowballPerk.class, 2);
        }else{
            PerkHandler.subscribe(this, ZombieWatcherPerk.class, 1);
        }
    }

    public void hideNameTag(){
        for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()) {
            if(JP == this || !JP.isAlive()){
                continue;
            }
            if(JP.hiddenNames.containsKey(this.getPlayer().getUniqueId())){
                PacketHelper.despawnEntity(JP.getPlayer(), JP.hiddenNames.remove(this.getPlayer().getUniqueId()));
            }
            EntityArmorStand stand = PacketHelper.setToolTipName(JP.getPlayer(), this.getPlayer(), this.getPlayer().getHealth() < this.getPlayer().getMaxHealth() ? this.NAMETAG_HURT : null);
            JP.addLoadedNameEntry(this.getPlayer(), stand);
        }
    }

    public void updateNameTag(Player p, String name){
        PacketHelper.updateHologram(this.getPlayer(), (EntityArmorStand) hiddenNames.get(p.getUniqueId()), name);
    }

    public void updateHealthFromPlayers(){
        int index = 8;
        if(!this.isSurvivor()){
            return; //juggie doesnt get to see stuff!
        }
        for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
            if(JP==this || !JP.isSurvivor()){
                continue; //dont show our own health of course, and not the juggernaut's either!
            }
            if(!JP.isAlive()){
                this.getPlayer().getInventory().setItem(index--, SkyItemHelper.getSkullStack(-6, JP.getPlayer()));
            }else {
                this.getPlayer().getInventory().setItem(index--, SkyItemHelper.getSkullStack((int) JP.getPlayer().getHealth(), JP.getPlayer()));
            }
        }
        while(index-- > 4){
            this.getPlayer().getInventory().setItem(index, null);
        }
    }

    public void resetNameTag(){
        System.out.println("RESETTING NAME TAG FOR PLAYER " + toString());
        killNameLoop = true;
        for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
            JP.removeNameEntry(this.getPlayer());
        }
    }

    public void die(){
        this.isAlive = false;
        killSpeedLoop = true;
        killSprintLoop = true;
        killFunctionTimer = true;
        player.setHealth(20);
        player.setFoodLevel(20);
        //
        for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
            if(JP.isAlive()){
                JP.updateHealthFromPlayers();
            }
        }
    }

    public boolean isAlive(){ return isAlive; }

    public void destroy(){
        HandlerList.unregisterAll(this);
        killSpeedLoop = true;
        killFunctionTimer = true;
        killSprintLoop = true;
        setSpeed(defaultSpeed);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.ADVENTURE);
        player.setExp(0f);
        player.setLevel(0);
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setWalkSpeed(0.2f);
//        for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
//            JP.getPlayer().showPlayer(player);
//        }
        player.setAllowFlight(false);
        player.setFlying(false);
        player.teleport(JuggernautGame.lobbySpawn);

        resetNameTag();
        resetPerks();
    }

    private void resetPerks(){
        for(AbstractPerk perk: perks){
            perk.disable();
            PerkHandler.unsubscribe(this, perk.getClass());
        }
    }

    @EventHandler
    public void onEntityHitByEntity(EntityDamageByEntityEvent e){
        if(e.getEntity().getUniqueId() == this.getPlayer().getUniqueId()){
            //Its our player who got damaged
            if(e.getDamager() instanceof Snowball && !this.isSurvivor()){
                changeSpeedMultiplier(-0.05d);
                e.setDamage(0);
                this.damagePlayer((2-calculateArmorValues(this)));
                System.out.println("Juggy got hit by snowball!");
                return;
            }
            JuggernautPlayer p2 = JuggernautGame.getInstance().getJuggernautPlayer(e.getDamager().getUniqueId());
            if(p2==null || !p2.isAlive() || !this.isAlive()){
                return; //dead players cant hurt people!
            }
            if(!this.isSurvivor()){
                changeSpeedMultiplier(-0.02d);
            }else{
                //System.out.println("Debug: hit by " + p2 + " :: " + e.getDamager().getUniqueId() + " :: " + e.getDamager().getName());
                if(p2!=null){ //should never be null, but hey!
                    if(p2.isSurvivor()){
                        e.setCancelled(true);
                        return;
                    }
                }else{
                    System.out.println("[Jugg] Error: Player null!"); //shouldnt ever happen, but lets keep it in.
                }
            }

            //calculate our custom damage according to myth's doc
            double damageToDeal = calculateDamage(p2, this);

            if(player.getHealth() <= damageToDeal){
                e.setDamage(0d);
                JuggernautGame.getInstance().killPlayer(this);
                return;
            }

            //lets damage the player for our own system!
            e.setDamage(0d);
            this.damagePlayer(damageToDeal);

            //if we get here, the player got damaged.
            //therefore, if its a survivor, we update their above-head text!
            //we also update the heads in hotbar for other players
            JuggernautGame.getInstance().getPlayers().forEach(JuggernautPlayer::updateHealthFromPlayers);
            if(this.isSurvivor()){
                setTextForOthers();
            }else{
                //jugg, lets give him the heal message
                sendDamagedText();
            }
        }
    }

    public void setSpeedMultiplier(double multiplier){
        currentSpeedMultiplier=multiplier;
        updateSpeed();
    }

    public void damagePlayer(double damage){
        this.getPlayer().setHealth(this.getPlayer().getHealth()-damage);
    }

    public double calculateAttack(JuggernautPlayer player){
        Material sword = player.getPlayer().getItemInHand().getType();
        double baseDamage = sword==Material.DIAMOND_SWORD ? 7 : sword==Material.IRON_SWORD ? 6 : sword==Material.STONE_SWORD ? 5 : sword==Material.WOOD_SWORD ? 4 : 2;
        return baseDamage;
    }

    public double calculateDamage(JuggernautPlayer damager, JuggernautPlayer victim){

//        System.out.println("Damage by sword (" + sword + ") : " + baseDamage);
        double baseAttack = calculateAttack(damager);
        double baseArmor = calculateArmorValues(victim);

        double finalDamage = baseAttack * (1d-baseArmor);
        return finalDamage;
    }

    public double calculateArmorValues(JuggernautPlayer player){
        double armor = 0d;
        if(!player.isSurvivor()){
            return 4*0.17d;
        }
        for(int chest = 1; chest < 5; chest++) {
            int chestBonus = player.getChestBonus(chest);
            armor += chestBonus==0? 0.07d : chestBonus==1 ? 0.1d : chestBonus==2 ? 0.13d : chestBonus == 3 ? 0.17d : 0;
        }
        return armor;
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event){
        if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
            return; // dont do stuff twice
        }
        if(event.getEntity().getUniqueId() == this.getPlayer().getUniqueId()){
            if(event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION){
                return; // no suffocation!
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    JuggernautGame.getInstance().getPlayers().forEach(JuggernautPlayer::updateHealthFromPlayers);

                }
            }.runTaskLater(Juggernaut.pluginInstance, 2L);
            if(player.getHealth() <= event.getFinalDamage()){
                event.setDamage(0d);
                JuggernautGame.getInstance().killPlayer(this);
                return;
            }
            if(this.isSurvivor()){
                setTextForOthers();
            }else{
                //jugg, lets give him the heal message
                sendDamagedText();
            }
        }
    }

    public void sendDamagedText(){
        if(isDamagedLoopRunning){
            return;
        }
        isDamagedLoopRunning = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(player.getHealth() >= player.getMaxHealth()){
                    //fully healed, lets reset this!
                    isDamagedLoopRunning = false;
                    PacketHelper.sendChatMessage(player, (byte) 2, "");
                    this.cancel();
                }
                PacketHelper.sendChatMessage(player, (byte) 2, "\u00a7cYou're damaged! Sneak to heal!");
            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L, 20L);
    }

    public void setTextForOthers(){
        if(isTextDamageLoopRunning){
            return;
        }
        isTextDamageLoopRunning = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(getPlayer().getHealth() >= getPlayer().getMaxHealth()){
                    for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
                        JP.updateNameTag(JP.getPlayer(), null);
                    }
                    this.cancel();
                    isTextDamageLoopRunning = false;
                    return;
                }
                for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
                    if(!JP.isSurvivor() || JP == JuggernautPlayer.this || !JP.isAlive()){
                        continue; // we dont want the juggernaut or himself to get this armorstand.
                    }
                    //System.out.println("UpdateThisNameTagHurt -- " + JP.toString());
                    if(SkyMathHelper.isInRadius(player.getLocation(), JP.getPlayer().getLocation(), maxDamageDisplayRadius, false)) {
                        JP.updateNameTag(getPlayer(), NAMETAG_HURT);
                    }else{
                        JP.updateNameTag(getPlayer(), null);
                    }
                }
            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L, 5L);

    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent event){
        if(event.getWhoClicked().getUniqueId()==this.getPlayer().getUniqueId()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNaturalRegen(EntityRegainHealthEvent event){
        if(event.getEntity().getUniqueId()==this.getPlayer().getUniqueId()){
            if(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent event){
        if(event.getEntity().getUniqueId() == this.getPlayer().getUniqueId()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        if(event.getPlayer().getUniqueId() == this.getPlayer().getUniqueId()){
            event.setCancelled(true);
        }
    }

    public double getSpeedMultiplier(){
        return currentMultiplierSpeed;
    }

    public void heal(double amount){
        if(this.player.getPlayer().getHealth() < this.player.getPlayer().getMaxHealth()) {
            if(this.player.getHealth() +amount > this.player.getPlayer().getMaxHealth()){
                this.player.getPlayer().setHealth(this.player.getPlayer().getMaxHealth());
            }else {
                this.player.getPlayer().setHealth(this.player.getHealth() + amount);
            }
            JuggernautGame.getInstance().getPlayers().forEach(JuggernautPlayer::updateHealthFromPlayers);
        }
        if(this.player.getHealth() >= this.player.getPlayer().getMaxHealth()){
            //if they got healed, we should make the armorstand for everyone cleared again!
            for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
                if(!JP.isSurvivor() || JP == this || !JP.isAlive()){
                    continue; // we dont want the juggernaut or himself to get this armorstand.
                }
                JP.updateNameTag(this.getPlayer(), null);
            }
        }
        //if its not below max health, we dont do stuff.
    }

    private void functionTimer(){
        new BukkitRunnable() {

            private int juggProgress = 0;

            @Override
            public void run() {
                if (killFunctionTimer) {
                    killFunctionTimer = false;
                    this.cancel();
                    return;
                }
                if(isSurvivor()) {
                    if (player.isSneaking()) {
                        double totalOccupations = 0;
                        ArrayList<JuggernautPlayer> toHeal = new ArrayList<>();
                        for (JuggernautPlayer JP : JuggernautGame.getInstance().getPlayers()) {
                            if (JP.isSurvivor() && player.getPlayer().getUniqueId() != JP.getPlayer().getUniqueId() && player.getPlayer().getLocation().distance(JP.getPlayer().getLocation()) <= 2) {
                                //its not us, and he's within range! (and a friendly)
                                if(JP.isDamaged()){
                                    toHeal.add(JP);
                                    totalOccupations++;
                                }
                               // JP.heal(0.05d);
                            }
                        }
                        ArrayList<AbstractSupplyChest> toClaim = new ArrayList<AbstractSupplyChest>();
                        ArrayList<AbstractSupplyChest> toOpen = new ArrayList<AbstractSupplyChest>();
                        for(AbstractSupplyChest chest: JuggernautGame.getInstance().getChests()){
                            if(SkyMathHelper.isInRadius(chest.getLocation(), player.getLocation(), chest.chestRadius, true)){
                                if(chest.isOpened()){
                                    if(getChestBonus(chest.getID())<chest.getLevel()){
                                        toClaim.add(chest);
                                        totalOccupations++;
                                    }
                                }else{
                                    toOpen.add(chest);
                                    totalOccupations++;
                                }
                            }
                        }
                        if(totalOccupations==0){
                            totalOccupations=1;
                        }
                        double partialOc = 1d/totalOccupations;
                        for(JuggernautPlayer JP: toHeal){
                            JP.heal(0.05d*partialOc);
                        }

                        for(AbstractSupplyChest chest: toClaim){
                            chest.increaseClaimProgress(JuggernautPlayer.this, partialOc);
                        }

                        for(AbstractSupplyChest chest: toOpen){
                            chest.dealChestDamage(partialOc);
                        }

                    }
                }else{
                    //JUGGERNAUT
                    if(player.isSneaking()){
                        double totalOccupations = 0;
                        ArrayList<AbstractSupplyChest> toClose = new ArrayList<AbstractSupplyChest>();
                        for(AbstractSupplyChest chest: JuggernautGame.getInstance().getChests()) {
                            if(SkyMathHelper.isInRadius(chest.getLocation(), player.getLocation(), chest.chestRadius, true)){
                                if(!chest.isOpened()){
                                    toClose.add(chest);
                                    totalOccupations++;
                                }
                            }
                        }

                        if(JuggernautPlayer.this.isDamaged()){
                            totalOccupations++;
                        }


                        double partialOc = 1d/totalOccupations;
                        heal(0.025d * partialOc); //if hes fully healed, wont matter

                        for(AbstractSupplyChest chest: toClose){
                            chest.dealChestDamage(-partialOc * 1.5d);
                        }
                    }
                }
            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L,2L);
    }

    public boolean isDamaged(){
        return player.getHealth() < player.getMaxHealth();
    }

    @EventHandler
    public void onServerLeave(PlayerQuitEvent event){
        if(event.getPlayer().getUniqueId() == this.getPlayer().getUniqueId()){
            if(this.isAlive()) {
                JuggernautGame.getInstance().killPlayer(this);
            }
        }
    }

    public AbstractPerk getPerkByClass(Class clazz){
        for(AbstractPerk perk: perks){
            if(perk.getClass() == clazz){
                return perk;
            }
        }
        return null;
    }

    @Override
    public String toString(){
        String end =  "\u00A7c" +player.getName() + " (" + (isAlive() ? "Alive)" : "Dead)") + ": \u00A73" + ((isSurvivor()) ? "Survivor" : "Juggernaut") + " \u00a7dSpeed%: " + currentMultiplierSpeed +
                " \u00a7cHealth: " + player.getHealth() + " \u00A73CurSpeed: " + player.getWalkSpeed() + " \u00a79Chestunlocks: \u00a77";
        for(Map.Entry<Integer, Integer> entry : claimedChestLevels.entrySet()){
            end += "("+ entry.getKey() + ": " + entry.getValue() + ") ";
        }
        return end;
    }
}

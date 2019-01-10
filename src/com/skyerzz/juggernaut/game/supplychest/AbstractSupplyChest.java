package com.skyerzz.juggernaut.game.supplychest;

import com.skyerzz.juggernaut.*;
import com.skyerzz.juggernaut.game.JuggernautGame;
import com.skyerzz.juggernaut.game.JuggernautPlayer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract class of supply chests.
 * Created by sky on 5-9-2018.
 */
public abstract class AbstractSupplyChest implements Listener{

    private int chestID;

    private Location location;
    private byte direction; //currently unused. Hasnt been bothering whilst playing the game. TODO maybe?
    private int level = 0; //curernt level of the chest
    private double secondsToUnlock = 15; //base seconds to unlock the chest
    private double unlockedPercentage = 0; //current percentage of the chest unlocked
    private float armorstandProgressbarHeight = 0.0f, armorstandItemHeight = 0.2f, armorstandClaimProgressHeight = 1.2f, armorstandInfoHeight = 0.4f, armorstandOpenedInfoHeight = 0.1f; //height standards for armorstand texts.
    public final int chestRadius = 2, nametagRadius = 10, particleRadius = 10, particlePoints = 60; //radius standards of particles/visibility
    protected String displayName = "AbstractSupplyChest", infoString = "\u00A76Shift to unlock!", juggyInfoLockString = "\u00A76Shift to Lock!", infoClaimString = "\u00A76Shift to Claim!"; //string standards for armorstand texts
    protected int armorstandProgressbarID, armorstandItemID, armorstandClaimProgressID, armorstandResetChestID, armorstandInfoID, armorstandClaimInfoID; //different for each chest, unique armorstand entity IDs
    private boolean shouldCancelRunnable = false; //indicator if the chest tick should(n't) be cancelled.
    private int chestProgressLevel = 0; // 0 = locked, 1 = unlocked, 2=locked&respawning
    private ItemStack firework; //firework when chest shoots one
    boolean didProgressionChange = false; //dirty bit to see if progression on the chest has changed, and it should update people in range.

    private HashMap<JuggernautPlayer, Double> claimProgress = new HashMap<>(); //people who are claiming the item from the chest
    private double secondsToClaimItem = 3; //base seconds to claim an item from an opened chest

    /**
     * Instances a new chest
     * @param chestID ID of the chest
     * @param startLocation Location of the chest
     * @param direction Direction of the chest (unused currently)
     */
    AbstractSupplyChest(int chestID, Location startLocation, byte direction){
        this.chestID = chestID;
        this.location = startLocation;
        this.direction = direction;


        //index the firework so it wont crash when trying to launch it, and it looks a lot cooler.
        indexFirework();


        this.location.getBlock().setType(Material.CHEST);//actually set the block to a chest
//        this.location.setX(this.location.getBlockX()+0.5f);
//        this.location.setY(this.location.getBlockY());
//        this.location.setZ(this.location.getBlockZ()+0.5f);

        Juggernaut.pluginInstance.getServer().getPluginManager().registerEvents(this, Juggernaut.pluginInstance); //lets also listen for events. could be neat.


        //start checking if players are near
        startLoop();

    }

    /**
     * Creates a beautiful firework the chest can use in the future.
     */
    private void indexFirework(){
        firework  = new ItemStack(Material.FIREWORK, 1);
        FireworkMeta meta = (FireworkMeta) firework.getItemMeta();

        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.LIME).build());
        meta.setPower(1);
        firework.setItemMeta(meta);
    }

    /**
     * Generic function to make an item unbreakable. No more durability needed
     * @param itemStack Itemstack to make unbreakable
     * @return Unbreakable Itemstack
     */
    public ItemStack makeUnbreakable(ItemStack itemStack){
        net.minecraft.server.v1_8_R3.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Unbreakable", true);
        item.setTag(tag);
        return CraftItemStack.asCraftMirror(item);
    }

    /**
     * Gets the maximum level the chest can reach
     * @return int maximum level
     */
    abstract int getMaxLevel();

    /**
     * Gets the chest state
     * @return true if the chest is unlocked, false if its not yet unlocked
     */
    public boolean isOpened(){
        return chestProgressLevel==1;
    }

    /**
     * Gets the chest ID
     * @return int chestID
     */
    public int getID(){
        return chestID;
    }

    /**
     * gets the current chest level
     * @return int level
     */
    public int getLevel(){
        return level;
    }

    /**
     * Main logic loop of the chest. Runs every 2 ticks.
     */
    private void startLoop(){
         new BukkitRunnable(){

            int lastFirework = 0, loopsTillFirework = (10*10); //2 ticks per loop, 10 seconds

            @Override
            public void run() {
                if(shouldCancelRunnable){ //if we should cancel the runnable, cancel immediately!
                    shouldCancelRunnable = false;
                    this.cancel();
                    return; //dont continue further, we're cancelled!
                }

                ArrayList<JuggernautPlayer> players = JuggernautGame.getInstance().getPlayers(); //get all ingame players
                if(chestProgressLevel==0) { //Logic if the chest is LOCKED
                    lastFirework = loopsTillFirework; //set the count for last firework to max, so it shoots instantly when opened. //todo this should be somewhere else for optimisation
                    boolean didProgressionUpdate = false; //dirty bit to see if we have updated any players
                    for (JuggernautPlayer jp : players) { //do this for every player

                        Player p = jp.getPlayer(); //get the actual player first

                        //particle logic: Check if player is in the radius of the chest to see particles. Ignore Y levels if player is no longer alive.
                        if (SkyMathHelper.isInRadius(location, p.getLocation(), (double) particleRadius, !jp.isAlive())) {
                            if(jp.isSurvivor() || unlockedPercentage>0){ //if the player is a survivor, or if its the juggernaut who CAN re-lock the chest, show particles!
                                displayParticles(p, chestRadius, ParticleColor.GREEN);
                            }
                        }

                        //hologram logics: check if player is in radius of the chest to see holograms. Ignore Y levels if player is dead.
                        if (SkyMathHelper.isInRadius(location, p.getLocation(), (double)nametagRadius, !jp.isAlive())) {

                            //if progression changed since the last update, and they already have the progressbar loaded, update their progress bar!
                            if (didProgressionChange && jp.getLoadedEntities().containsKey(armorstandProgressbarID)) {
                                didProgressionUpdate = true; //set dirty bit to indicate we have done updates required
                                jp.updateHologram(armorstandProgressbarID, getProgressBar()); //update the actual hologram
                            }

                            //if the player did not yet have the progressbar loaded, load it to them!
                            else if (!jp.getLoadedEntities().containsKey(armorstandProgressbarID)) {
                                //always give everyone the progressbar hologram
                                jp.displayHologram(location.getX(), location.getY() + armorstandProgressbarHeight, location.getZ(), getProgressBar(), armorstandProgressbarID);

                                //show info text depending on if the player is survivor or juggernaut.
                                if(jp.isSurvivor()) {
                                    jp.displayHologram(location.getX(), location.getY() + armorstandInfoHeight, location.getZ(), infoString, armorstandInfoID);
                                }else if(unlockedPercentage > 0){ //only show the info to the juggernaut if he can actually relock it!
                                    //its juggy and he can re-lock the chest!
                                    jp.displayHologram(location.getX(), location.getY() + armorstandInfoHeight, location.getZ(), juggyInfoLockString, armorstandInfoID);
                                }
                            }
                        } else { //the player is NOT in radius of the chest to see holograms. Therefore, we delete their hologram!
                            jp.removeEntities(armorstandProgressbarID, armorstandInfoID);
                        }
                    }
                    if(didProgressionUpdate){ //we updated the players succesfully. We can turn off the progressionchange dirty bit again.
                        didProgressionChange = false;
                    }
                }else if(chestProgressLevel==1) {//its unlocked, we give people items now!
                    boolean showFirework = lastFirework++ > loopsTillFirework; //check if we should show a firework
                    if(showFirework){
                        lastFirework = 0; //reset the lastfirework count
                    }
                    for (JuggernautPlayer jp : players) { //lets check each player for updates to holograms.
                        Player p = jp.getPlayer();//get the player

                        //check if they are in radius to show the armorstand tags (ignore Y levels)
                        if(SkyMathHelper.isInRadius(location, p.getLocation(), (double) nametagRadius, false)){
                            //load the floating item (if they dont have it loaded yet)
                            if(!jp.getLoadedEntities().containsKey(armorstandItemID)){
                                jp.displayFloatingItem(location.getX(), location.getY()+armorstandItemHeight, location.getZ(),new net.minecraft.server.v1_8_R3.ItemStack(Item.getById(getItemID(level))), armorstandItemID);
                            }
                            //same for the text
                            if(!jp.getLoadedEntities().containsKey(armorstandClaimInfoID) && jp.isSurvivor()){
                                jp.displayHologram(location.getX(), location.getY()+armorstandOpenedInfoHeight, location.getZ(), infoClaimString, armorstandClaimInfoID);
                            }

                            //todo there might be optimisation here. This function call seems unneccesary. Try to remove in future update.
                            updatePlayerClaim();
                        }else{
                            //they're not in range, so we remove all armorstands related.
                            jp.removeEntities(armorstandClaimProgressID, armorstandItemID, armorstandClaimInfoID);
                        }

                        if(jp.getChestBonus(chestID)>=level){
                            //they already unlocked this bonus, so we dont really need to show them fireworks anymore.
                            //todo still check if in range.
                            continue;
                        }

                        //they havent claimed it yet, and we can show them a firework! PFFIIIEEEW!
                        if(showFirework){
                            PacketHelper.displayFirework(p, location.getX(), location.getY()+1, location.getZ(), CraftItemStack.asNMSCopy(firework));
                        }

                        if(!jp.isSurvivor()){
                            //we dont want juggy's claiming stuff!
                            continue;
                        }

                        //show them some nice lightblue particles to indicate where they have to sneak to claim it.
                        if( SkyMathHelper.isInRadius(location, p.getLocation(), (double)particleRadius, false)) {
                            displayParticles(p, chestRadius, ParticleColor.LIGHT_BLUE);
                        }
                    }
                }

            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L, 2L); //run every 2 ticks
    }

    /**
     * Updates this chest
     */
    public void update(){
        if(this.chestProgressLevel==1) { //if its unlocked, update the player claims.
            updatePlayerClaim();
        }
    }

    /**
     * Gets the location of this chest
     * @return Location of chest
     */
    public Location getLocation(){
        return location;
    }

    /**
     * Increases the claim progress for the given player with the partial value
     * @param player Player to increase progress for
     * @param partialValue Value of incrementation
     */
    public void increaseClaimProgress(JuggernautPlayer player, double partialValue){
        if(!player.isAlive()){ //if the player is not alive, we dont want him here.
            return;
        }

        //get the current progress
        double currentProgress;
        if(claimProgress.containsKey(player)) {
            currentProgress = claimProgress.get(player);
        }else{
            currentProgress = 0d;
        }

        //add progress depending on how long it takes to claim an item, and the given partialValue.
        currentProgress += (((double)100/secondsToClaimItem)/10)*partialValue;

        //if the progress is 100% (or higher), and the player has NOT yet claimed this (failsafe), give them the claim!
        if(currentProgress>=100 && player.getChestBonus(chestID)<level){
            player.setChestBonus(chestID, level);
            applyItem(player, level);

            //reset experience bar
            player.getPlayer().setLevel(0);
            player.getPlayer().setExp(0f);

            //update the playerclaims
            updatePlayerClaim();
        }else{
            //its not 100% claimed yet, so we just update the claim progress
            claimProgress.put(player, currentProgress);

            //update their experience bar/level to indicate how far they are.
            player.getPlayer().setLevel((int) currentProgress);
            player.getPlayer().setExp((float) (currentProgress/100d));
            updatePlayerClaim(); //todo this may not be of any influence here.
        }
    }

    /**
     * Deletes the chest fully
     */
    public void deleteInstance(){
        //cancels the runnable
        shouldCancelRunnable = true;
        //remove the chest from the map to avoid confusion
        this.location.getBlock().setType(Material.AIR);
        //unregister from all events, we're done!
        HandlerList.unregisterAll(this);
    }

    /**
     * Updates how many players claimed the chest, and respawns the chest if all players claimed.
     * includes a loop which counts down the respawn.
     */
    private void updatePlayerClaim(){
        if(chestProgressLevel!=1){
            return; //the chest isnt unlocked!
        }

        //count the total players, and the players who unlocked it.
        int totalPlayers = 0;
        int unlockedPlayers = 0;
        for(JuggernautPlayer player: JuggernautGame.getInstance().getPlayers()){
            if(!player.isSurvivor() || !player.isAlive()){
                continue; //they be dead, they dont count! (Juggy also doesnt count, he doesnt get to unlock items)
            }
            totalPlayers++; //+1 total players
            if(player.isSurvivor() && player.getChestBonus(chestID)>=level){
                unlockedPlayers++; //they unlocked it, +1 unlocked players
            }
        }

        //all players unlocked it
        if(totalPlayers<=unlockedPlayers && chestProgressLevel==1){
            //lock the chest before respawning.
            lockChest();
            new BukkitRunnable() {

                int timeTillRespawn = 3; //3 seconds to respawn a chest
                @Override
                public void run() {
                    //runs every second
                    for(JuggernautPlayer jp : JuggernautGame.getInstance().getAllPeople()){
                        //check if the player is in the radius of the chest (ignore Y)
                        if(SkyMathHelper.isInRadius(location, jp.getPlayer().getLocation(), nametagRadius, false)) {
                            //show them the hologram. Update if they already have the hologram active.
                            //If this chest will not respawn, we tell them "Good Luck!" instead of the respawning message.
                            if (jp.getLoadedEntities().containsKey(armorstandResetChestID)) {
                                if(level==getMaxLevel()){
                                    jp.updateHologram(armorstandResetChestID, "\u00A76Good Luck!");
                                }else {
                                    jp.updateHologram(armorstandResetChestID, "\u00A76Respawning in " + timeTillRespawn);
                                }
                            } else {
                                if(level==getMaxLevel()) {
                                    jp.displayHologram(location.getX(), location.getY() + armorstandProgressbarHeight, location.getZ(), "\u00A76Good Luck!", armorstandResetChestID);
                                }else{
                                    jp.displayHologram(location.getX(), location.getY() + armorstandProgressbarHeight, location.getZ(), "\u00A76Respawning in " + timeTillRespawn, armorstandResetChestID);
                                }
                            }
                        }else{
                            //they arent in range, so we remove this entity for them
                            jp.removeEntities(armorstandResetChestID);
                        }
                    }

                    //check if its time to respawn.
                    if(timeTillRespawn--<=0){
                        //its time! respawn the chest!
                        respawnChest();
                        for(JuggernautPlayer jp: JuggernautGame.getInstance().getPlayers()){
                            //remove the countdown armorstand for all players
                            jp.removeEntities(armorstandResetChestID);
                        }
                        this.cancel(); //cancel this runnable, we no longer need it.
                        return;
                    }
                }
            }.runTaskTimer(Juggernaut.pluginInstance, 0L, 20L); //every 20 ticks (1s)
        }else{
            //not all players unlocked the item yet. Lets indicate how many did.
            String emptyDisplayText = unlockedPlayers + "/" + totalPlayers + " Claimed";
            for(JuggernautPlayer JP: JuggernautGame.getInstance().getPlayers()){
                if(!SkyMathHelper.isInRadius(location, JP.getPlayer().getLocation(), nametagRadius, false)){
                    //they arent in the radius, so we remove their armorstand again (if they had it).
                    if(JP.getLoadedEntities().containsKey(this.armorstandClaimProgressID)) {
                        JP.removeEntities(armorstandClaimProgressID);
                    }
                    continue;
                }

                //generate the color of the display text we need to show them. green if they claimed it, otherwise red.
                String displayText;
                if(JP.getChestBonus(chestID)>=level){
                    displayText = "\u00A72" + emptyDisplayText;
                }else{
                    displayText = "\u00A7c" + emptyDisplayText;
                }

                //update, or show the hologram to them.
                if(JP.getLoadedEntities().containsKey(this.armorstandClaimProgressID)){
                    JP.updateHologram(armorstandClaimProgressID, displayText);
                }else{
                    JP.displayHologram(this.location.getX(), this.location.getY()+this.armorstandClaimProgressHeight, this.location.getZ(), displayText, armorstandClaimProgressID);
                }
            }
        }
    }

    /**
     * Respawns the chest in a new random location
     */
    private void respawnChest(){
        //remove the physical chest
        this.location.getBlock().setType(Material.AIR);

        //increment the level of the chest. If its higher than its max level, shut down.
        if(++this.level >= getMaxLevel()){
            deleteInstance();
            return;
        }

        //play a sound effect for all players (INCLUDING juggernaut) to indicate a chest respawned.
        for(JuggernautPlayer jp: JuggernautGame.getInstance().getPlayers()){
            PacketHelper.playSound(jp.getPlayer(), location, Sound.DIG_WOOD, 1f, 1f);
        }

        //set the seconds to unlock depending on the level of the chest
        this.secondsToUnlock = this.level==0 ? 15 : this.level==1 ? 30 : 60;

        //reset all old values
        this.claimProgress.clear();
        this.unlockedPercentage = 0;
        this.chestProgressLevel = 0;

        //get a new location to spawn the new chest in.
        this.location = JuggernautGame.getInstance().getRandomChestLocation().use();

        //log
        System.out.println("[Jugg] Respawned chest " + this.getClass().getSimpleName() + " Level " + this.level + " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());

        //set the new location to a chest block
        this.location.getBlock().setType(Material.CHEST);
    }

    /**
     * Generates a progress bar for unlocking the chest
     * @return String progressbar
     */
    private String getProgressBar(){
        String output = "\u00A77[";
        int total = 20;
        double unlocked = this.unlockedPercentage;
        output += "\u00A72";
        //green bars for every 5% unlocked
        while(unlocked>=5){
            output += "|";
            unlocked-=5;
            total--;
        }
        //rest of them are red bars
        output += "\u00A7c";
        while(total-->0){
            output += "|";
        }

        output += "\u00A77]";
        return output;
    }

    /**
     * Displays circular particles to the given player with given radius from the location of this chest
     * @param p Player to show the particles
     * @param radius Radius of the particles
     * @param color Color of the particles
     */
    private void displayParticles(Player p, double radius, ParticleColor color){
        //just some math to create a circle, and send it to the player. z+0.1f so they arent inside of the bottom block.
        double part = 2 * Math.PI / particlePoints;
        for (int i = 0; i < particlePoints; i++)
        {
            double alpha = part * i;
            float x = (float) (location.getX() + radius * Math.cos(alpha));
            float z = (float) (location.getZ() + radius * Math.sin(alpha));
            float y = (float) (location.getY()+0.1f);
            PacketHelper.displaySingleParticle(p, x, y, z, color);
        }
    }

    /**
     * Deals given amount of damage to the chest to unlock it.
     * @param amount
     */
    public void dealChestDamage(double amount){
        if(amount==0){ //if its exactely 0, we dont need to do anything. Not <= since juggernaut can lock chests, giving a negative damage.
            return;
        }
        didProgressionChange =true; //dirty bit: we did change the progression if it got here.

        //some math to work correctly with the base secondsToUnlock chest. Calculates the percentage it needs to add.
        double donePercentage = (((double)100/secondsToUnlock)/10) * amount;
        this.unlockedPercentage = Math.min(100, unlockedPercentage+donePercentage); //safeguard to never go over 100%

        //if we're at 100%, and the chest is locked, unlock the chest
        if (unlockedPercentage >= 100 && chestProgressLevel==0) {
            synchronized (this) { //to prevent issues with multiple people unlocking a chest at the same time, we synchronize this to be sure it only happens ONCE
                if (unlockedPercentage >= 100 && chestProgressLevel==0) { //for the above reason, double check.
                    System.out.println("[Jugg] Dealt 100% damage to chest " + this.getClass().getSimpleName() + " : Level " + level); //log
                    chestProgressLevel = 1; //set the chest state to UNLOCKED.
                    unlockChest(); //unlock the chest.
                    return;
                }
            }
        }

        //safeguard to make sure the juggernaut never makes a chest go into the negatives.
        if (unlockedPercentage < 0) {
            unlockedPercentage = 0;
        }
    }

    /**
     * Unlocks the chest
     */
    private void unlockChest(){
        chestProgressLevel = 1; //set the chest state to UNLOCKED
        for(JuggernautPlayer player: JuggernautGame.getInstance().getPlayers()){
            //opens the chest visually
            player.updateChestState(this.location, true);
            //gives all players the sound (including juggernaut)
            PacketHelper.playSound(player.getPlayer(), location, Sound.CHEST_OPEN, 1f, 100f);
            //shows a floaty item thats in this chest
            player.displayFloatingItem(this.location.getX(), this.location.getY()+armorstandItemHeight, this.location.getZ(),new net.minecraft.server.v1_8_R3.ItemStack(Item.getById(getItemID(level))), armorstandItemID);
            //removes the armorstandInfo and progressbar entities
            player.removeEntities(armorstandProgressbarID, armorstandInfoID);
        }
        //log
        System.out.println("[Jugg] " + this.getClass().getSimpleName() + " level " + level + " unlocked!");
        updatePlayerClaim(); //to instance the 0/X claimed bar first time
    }

    /**
     * Locks the chest
     */
    private void lockChest(){
        //log
        System.out.println("[Jugg] "+ this.getClass().getSimpleName() + " level " + level + " locked!");

        //set the chest state to RESPAWNING
        this.chestProgressLevel = 2;
        for(JuggernautPlayer player: JuggernautGame.getInstance().getPlayers()){
            //close the chest
            player.updateChestState(this.location, false);
            //play the sound of the chest closing to everyone
            PacketHelper.playSound(player.getPlayer(), location, Sound.CHEST_CLOSE, 1f, 100f);
            //remove all unneeded armorstands again
            player.removeEntities(armorstandClaimProgressID, armorstandItemID, armorstandClaimInfoID, armorstandInfoID, armorstandProgressbarID);
        }
    }

    /**
     * Kills all armorstands this chest can possibly have for the given player
     * @param player Player to kill all armorstands for
     */
    public void killAllArmorstands(JuggernautPlayer player){
        player.removeEntities(armorstandProgressbarID, armorstandItemID, armorstandClaimProgressID, armorstandResetChestID, armorstandInfoID, armorstandClaimInfoID);
    }

    /**
     * Compares the location of this chest to the location given. Only uses block locations
     * @param location Location to compare
     * @return true if the location is the same
     */
    private boolean compareLocation(Location location){
        return location.getBlockX()==this.location.getBlockX() && location.getBlockY()==this.location.getBlockY() && location.getBlockZ() == this.location.getBlockZ();
    }

    /**
     * Blocks players from opening the chest by right-clicking
     * @param e PlayerInteractEvent
     */
    @EventHandler
    public void onChestOpen(PlayerInteractEvent e){
        if(e.getClickedBlock()==null){
            return; //they clicked air
        }
        if(compareLocation(e.getClickedBlock().getLocation())){
            e.setCancelled(true); //its our location they clicked, so we stop it.
        }
    }

    /**
     * Returns the ItemID for the given level of the chest
     * @param level Level of the chest
     * @return integer ItemID
     */
    abstract int getItemID(int level);

    /**
     * Applies an item from chest level to the player
     * @param player Player to apply the item to
     * @param level Level of the chest to apply
     */
    abstract void applyItem(JuggernautPlayer player, int level);

}

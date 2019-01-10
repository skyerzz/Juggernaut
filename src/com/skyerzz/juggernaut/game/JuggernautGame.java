package com.skyerzz.juggernaut.game;

import com.skyerzz.juggernaut.Juggernaut;
import com.skyerzz.juggernaut.SkyMathHelper;
import com.skyerzz.juggernaut.game.perk.EnderPearlPerk;
import com.skyerzz.juggernaut.game.perk.PerkHandler;
import com.skyerzz.juggernaut.game.perk.SnowballPerk;
import com.skyerzz.juggernaut.game.perk.SpeedBoostPerk;
import com.skyerzz.juggernaut.game.supplychest.*;
import com.skyerzz.juggernaut.map.ChestLocation;
import com.skyerzz.juggernaut.map.Map;
import com.skyerzz.juggernaut.map.MapBuilder;
import com.skyerzz.juggernaut.map.PlayerLocation;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Main class for a juggernaut game
 * Created by sky on 5-9-2018.
 */
public class JuggernautGame implements Listener{

    /** keep track of players, spectators and supply chests */
    private ArrayList<JuggernautPlayer> players = new ArrayList<>();
    private ArrayList<JuggernautSpectator> specs = new ArrayList<>();
    private ArrayList<AbstractSupplyChest> supplyChests = new ArrayList<>();

    /** lobby location */
    public static final Location lobbySpawn = new Location(Bukkit.getWorlds().get(0), 4965.5d, 4.1d, 52.5d, 0f, 0f);

    /** Game instance. null if no game is running */
    private static JuggernautGame instance;
    boolean hasStarted = false;

    //maximum amount of players INCLUDING juggernaut.
    private int maxPlayers = 7;

    //boolean to check if the game has already been initialised.
    private boolean isInitialised = false;

    //current Map
    private Map map;

    /**
     * Constructs a new game instance
     */
    private JuggernautGame(){
        //listen to events, we are going to need that.
        Bukkit.getServer().getPluginManager().registerEvents(this, Juggernaut.pluginInstance);
        System.out.println("[Jugg] Created new game instance!"); //log
    }

    /**
     * Returns the juggernaut game instance. If none exists, it creates one.
     * @return
     */
    public static JuggernautGame getInstance(){
        if(instance==null){
            instance = new JuggernautGame();
        }
        return instance;
    }

    /**
     * Returns a list with all spectators. List is empty if no spectators are in this game
     * @return ArrayList of JuggernautSpectator classes
     */
    public ArrayList<JuggernautSpectator> getSpectators(){
        return specs;
    }

    /**
     * Returns a list of both players and spectators in this game
     * @return ArrayList of JuggernautPlayers
     */
    public ArrayList<JuggernautPlayer> getAllPeople(){
        ArrayList<JuggernautPlayer> toReturn = (ArrayList<JuggernautPlayer>) players.clone(); //clone the game list
        for(JuggernautSpectator spec: specs){
            toReturn.add(spec); //add specs (these arent in the game list normally)
        }
        return toReturn;
    }

    /**
     * Adds a spectator to the game
     * @param JP JuggernautSpectator to add
     */
    public void addSpectator(JuggernautSpectator JP){
        if(!hasStarted){ //if the game hasnt started yet, we cant add spectators!
            return;
        }
        for(JuggernautPlayer JPL: players){
            if(JPL.getPlayer().getUniqueId() == JP.getPlayer().getUniqueId()){
                return; // he's already ingame as a regular player
            }
        }
        for(JuggernautPlayer JPS: specs){
            if(JPS.getPlayer().getUniqueId() == JP.getPlayer().getUniqueId()){
                return; //he's already a spec
            }
        }

        //add him to our list, and activate him
        specs.add(JP);
        JP.activate();
    }

    /**
     * Resets a game
     */
    public void reset(){
        System.out.println("[Jugg] Resetting game..."); //log
        map = null; //reset the map.
        instance = this; //instead of creating a full new instance, just set this one again TODO this can possibly be removed

        //for each chest that we have, kill all entities on active players, and delete the chest
        for(AbstractSupplyChest chest : supplyChests){
            for(JuggernautPlayer player: players) {
                chest.killAllArmorstands(player);
            }
            chest.deleteInstance();
        }
        supplyChests.clear();

        //destroy all players, and clear the list
        for(JuggernautPlayer player: players){
            player.destroy();
        }
        players.clear();

        //destroy all spectators, and clear the list.
        for(JuggernautSpectator spec: specs){
            spec.destroy();
        }
        specs.clear();

        //turn the gamestate back to the beginning
        hasStarted = false;
    }

    /**
     * initalises the game, including building a map
     */
    public void initialise(){
        System.out.println("[Jugg] initialising game..."); //log

        //creates a new mapBuilder
        MapBuilder builder = new MapBuilder(5000, 4, 0, 15);

        //send some messages to players to indicate what's happening, and build the actual map.
        Bukkit.broadcastMessage("[Juggernaut] Creating map... (This might lag a bit)");
        builder.startBuild();
        map = builder.getMap();
        Bukkit.broadcastMessage("[Juggernaut] Map created!");

        //kill item entities in the world left by the previous map
        Bukkit.getWorlds().get(0).getEntitiesByClass(Item.class).forEach(Entity::remove);

        //set the initialised state
        isInitialised = true;
    }

    /**
     * Adds a player to the game
     * @param p Player to add
     * @return True if successful
     */
    public boolean addPlayer(Player p){
        if(hasStarted || players.size() >= maxPlayers){
            //if the game is already ongoing, or there are already maximum players in the game, deny them access
            return false;
        }

        //if the player is already in the game himself, deny access
        for(JuggernautPlayer JP: players){
            if(JP.getPlayer().getUniqueId() == p.getUniqueId()){
                return false;
            }
        }

        //for all players ingame, send a message that he joined the game
        for(JuggernautPlayer JP: players){
            JP.getPlayer().sendMessage(p.getName() + " Joined the game!");
        }

        //log
        System.out.println("[Jugg] " + p.getName() + " Joined the game!");

        //return the value when added to the player list.
        return players.add(new JuggernautPlayer(p));
    }

    /**
     * Returns a list of all players in the game
     * @return ArrayList of JuggernautPlayers ingame
     */
    public ArrayList<JuggernautPlayer> getPlayers(){
        return players;
    }

    /**
     * Finds a player by UUID
     * @param uuid UUID of player to find
     * @return JuggernautPlayer if found, otherwise null
     */
    public JuggernautPlayer getJuggernautPlayer(UUID uuid){
        for(JuggernautPlayer p: players){
            if(p.getPlayer().getUniqueId() == uuid){
                return p;
            }
        }
        return null;
    }

    /**
     * Kills off the given player
     * @param player JuggernautPlayer to kill
     */
    public void killPlayer(JuggernautPlayer player){
        //broadcast a dying message
        Bukkit.broadcastMessage(player.getPlayer().getName() + " died!");
        //set the gamemode of the player to spectator, and kill him off
        player.getPlayer().setGameMode(GameMode.SPECTATOR);
        player.die();

        //for each chest we have, make them update with the new gamestate
        for(AbstractSupplyChest chest: supplyChests){
            chest.update();
        }

        //test if the game has been won by someone
        testWin();
    }

    /**
     * Tests, and hands out, the win if the game has been won by either team.
     */
    private void testWin(){
        //count the players still alive, INCLUDING juggernaut
        int alivePlayers = 0;
        for(JuggernautPlayer JP: players){
            System.out.println(JP.toString());
            if(JP.isAlive()){
                alivePlayers++;
            }
        }

        //check if the juggernaut has won the game
        boolean shouldReset = false;
        if(alivePlayers == 1){ //juggernaut can only win if he's the only one left alive
            for(JuggernautPlayer JP: players){
                if(JP.isAlive() && !JP.isSurvivor()){
                    //the one person alive is not a survivor, therefore the juggernaut wins.
                    Bukkit.broadcastMessage("[Juggernaut] \u00A7aJuggernaut wins the game!");
                    System.out.println("[Jugg] Juggernaut wins the game"); //log
                    shouldReset = true;
                    break;
                }
            }
            if(shouldReset){
                //reset the game if the juggernaut has won.
                reset();
                return;
            }
        }

        //check if survivors won the game
        boolean foundJuggernaut = false;
        for(JuggernautPlayer JP: players){
            if(JP.isAlive() && !JP.isSurvivor()){
                foundJuggernaut = true; //juggernaut is alive, therefore the survivors have not won (yet)
                break;
            }
        }
        if(!foundJuggernaut){
            //players win!
            Bukkit.broadcastMessage("[Juggernaut] \u00A7aSurvivors win the game!");
            System.out.println("[Jugg] Survivors wins the game"); //log
            reset();
        }
    }

    /**
     * Gets the Juggernaut
     * @return JuggernautPlayer who is the juggernaut, null if he doesnt exist
     */
    public JuggernautPlayer getJuggernaut(){
        for(JuggernautPlayer jp: getPlayers()){
            if(!jp.isSurvivor()){
                return jp;
            }
        }
        return null;
    }

    /**
     * Gets a random chest location on the map, which has not yet been used.
     * @return unused ChestLocation at random
     */
    public ChestLocation getRandomChestLocation(){
        if(map==null){
            return null;
        }
        ChestLocation loc =  map.getRandomChestLocation();
        return loc;
    }

    /**
     * Gets a random playerlocation on the map, which has not yet been used.
     * @return unused PlayerLocation at random
     */
    public PlayerLocation getRandomPlayerLocation(){
        PlayerLocation loc = map.getRandomPlayerLocation();
        return loc;
    }

    /**
     * Starts the countdown till the game
     */
    public void start(){
        //check if we have enough players
        if(players.size()<2){
            System.out.println("ERROR while starting game: less than 2 players!");
            return;
        }

        //if the game is already initialised, dont re-initialise it.
        if(!isInitialised) {
            initialise();
        }
        isInitialised = false; //reset it again when the game is running.


        //count down till the start
        new BukkitRunnable() {

            int timeTillStart = 5;
            @Override
            public void run() {
                //runs every second

                if(timeTillStart--<=0){
                    //countdown has ended, lets turn on the gamestate and start the actual game!
                    hasStarted = true;
                    this.cancel();
                    startGame();
                }else{
                    //messages & log
                    for(JuggernautPlayer jp : JuggernautGame.getInstance().getPlayers()){
                        jp.getPlayer().sendMessage("\u00a7aGame starts in " + (timeTillStart+1) + " seconds!");
                    }
                    System.out.println("[Jugg] Game starts in " + (timeTillStart+1) + " seconds!");
                }
            }
        }.runTaskTimer(Juggernaut.pluginInstance, 0L, 20L); //loop every second
    }

    /**
     * Starts the actual game
     */
    public void startGame(){
        if(players.size()<2){
            System.out.println("ERROR: less than 2 players in players list!");
            return;
        }

        //set 1 player to juggernaut
        Random r = new Random();
        players.get(r.nextInt(players.size())).setSurvivor(false);

        //index chests
        supplyChests.add(new BootSupplyChest(getRandomChestLocation().use(), (byte) 0));
        supplyChests.add(new ChestplateSupplyChest(getRandomChestLocation().use(), (byte) 0));
        supplyChests.add(new HelmetSupplyChest(getRandomChestLocation().use(), (byte) 0));
        supplyChests.add(new LeggingsSupplyChest(getRandomChestLocation().use(), (byte) 0));
        supplyChests.add(new SwordSupplyChest(getRandomChestLocation().use(), (byte) 0));

        //send people messages! People love messages.
        sendMessages();

        //lets actually teleport them to the map too.
        JuggernautPlayer Juggy = getJuggernaut();
        teleportPlayers(Juggy);

        for(JuggernautPlayer player: players){
            player.activate();
        }

        //initialise all perks so they actually work
        PerkHandler.initialiseAll();

        //stop listening to things here, we no longer need that as all that logic is inside of the Player, Chest and Perk classes.
        HandlerList.unregisterAll(this);
    }

    /**
     * Teleports the players into the game
     * @param juggy JuggernautPlayer who is the juggernaut
     */
    public void teleportPlayers(JuggernautPlayer juggy){
        //get a random location for the juggernaut
        Location juggLoc = getRandomPlayerLocation().use();
        juggy.getPlayer().teleport(juggLoc);

        //log + debug for finding potential errors on smaller maps with minimum distance tracking
        System.out.println("Teleporting players!");
        System.out.println("Jugg: " + juggLoc.getBlockX() + ", " + juggLoc.getBlockY() + ", " + juggLoc.getBlockZ());

        for(JuggernautPlayer player: players){
            if(!player.isSurvivor()){
                //we already teleported the juggernaut, if we find him we ignore him.
                continue;
            }

            //re-roll the playerLocation untill you find one who is at least 25 blocks away from the juggernauts spawnlocation.
            PlayerLocation loc;
            do {
                loc = getRandomPlayerLocation();
            }while(SkyMathHelper.isInRadius(juggLoc, loc.getLoc(), 25, false));

            //debug for potental errors on smaller maps
            System.out.println("Using location for player " + player.getPlayer().getName() + "!");

            //teleport if we found a location, otherwise, give a console error.
            if(loc!=null){
                player.getPlayer().teleport(loc.use());
            }else{
                System.out.println("ERROR: couldnt find any player locations!");
                continue;
            }
        }
    }

    /**
     * Sends messages to all players to indicate what team they're on
     */
    private void sendMessages(){
        for(JuggernautPlayer p: players){
            if(p.isSurvivor()){
                p.getPlayer().sendMessage("\u00A7aYou're a survivor! Work together to destroy the juggernaut!");
            }else{
                p.getPlayer().sendMessage("\u00A7cYou're the Juggernaut! Kill all survivors before they kill you!");
            }
        }
    }

    /**
     * Gets a list of all chests in game
     * @return ArrayList of AbstractSupplyChest
     */
    public ArrayList<AbstractSupplyChest> getChests(){
        return supplyChests;
    }

    /**
     * Stops people damaging each-other in the pregame lobby
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler
    public void damage(EntityDamageByEntityEvent event){
        //cancel ALL damage in the pregame lobby to prevent people killing eachother prematurely.
        event.setCancelled(true);
    }

    /**
     * Listens to people leaving the server, and destroys them
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onServerLeave(PlayerQuitEvent event){
        JuggernautPlayer JP = getJuggernautPlayer(event.getPlayer().getUniqueId());
        if(JP!=null){
            //one of ours left, lets destroy him properly, and give a notification to people.
            JP.destroy();
            this.getPlayers().remove(JP);
            System.out.println("[Jugg] " + event.getPlayer().getName() + "  left the game.");
        }
    }
}

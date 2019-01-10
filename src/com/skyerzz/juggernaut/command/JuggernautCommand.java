package com.skyerzz.juggernaut.command;

import com.skyerzz.juggernaut.Juggernaut;
import com.skyerzz.juggernaut.PacketHelper;
import com.skyerzz.juggernaut.game.JuggernautGame;
import com.skyerzz.juggernaut.game.JuggernautPlayer;
import com.skyerzz.juggernaut.game.JuggernautSpectator;
import com.skyerzz.juggernaut.game.perk.mobs.MobEntities;
import com.skyerzz.juggernaut.game.perk.mobs.WatcherZombie;
import com.skyerzz.juggernaut.game.supplychest.ChestplateSupplyChest;
import com.skyerzz.juggernaut.map.MapBuilder;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.Item;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by sky on 4-9-2018.
 */
public class JuggernautCommand implements CommandExecutor {

    /** prefix general */
    private static final String prefix = "[Jugg] ";


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length < 1){
            return false;
        }
        //codes: A = op user(admin), P = player, -C = console, -U = underlying, -T = testing purposes

        //<editor-fold desc="[P -T -C][testing commands]">
        //gives the current version of juggernaut
        if(args[0].equalsIgnoreCase("version")){
            sender.sendMessage(prefix + "Juggernaut is running version " + Juggernaut.version);
            return true;
        }
        //TODO remove eventually
        //spawns a watcherzombie at the location of the sender
        if(args[0].equalsIgnoreCase("zombie") && sender.isOp()){
            Player p = (Player) sender;
            MobEntities.spawnEntity(new WatcherZombie(p.getLocation()), p.getLocation());
            return true;
        }
        //</editor-fold>

        //<editor-fold desc="[A -C]">
        //Hides the name of the given player to all other ingame players
        if(args[0].equalsIgnoreCase("hideName") && sender.isOp()){
            if(args.length < 2){
                sender.sendMessage(prefix + "\u00a7cUsage: / jugg hideName <Player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if(target==null || !target.isOnline()){
                sender.sendMessage(prefix + "\u00A7cCouldn't find that player!");
                return true;
            }
            JuggernautPlayer thisplayer = JuggernautGame.getInstance().getJuggernautPlayer(target.getUniqueId());
            if(thisplayer==null){
                sender.sendMessage(prefix + "\u00a7cThis player is not in-game!");
                return true;
            }
            thisplayer.hideNameTag();
            sender.sendMessage(prefix + "\u00a76Hidden nametag for player " + args[1]);
            return true;
        }
        //Gives you debug information about all players/spectators
        if(args[0].equalsIgnoreCase("debug") && sender.isOp()){
            sender.sendMessage("Players:");
            for(JuggernautPlayer juggP: JuggernautGame.getInstance().getPlayers()){
                sender.sendMessage(juggP.toString());
            }
            String specsMsg = "";
            for(JuggernautSpectator juggSpec: JuggernautGame.getInstance().getSpectators()){
                specsMsg += juggSpec.getPlayer().getName() + ",";
            }
            sender.sendMessage("Spectators: " + specsMsg);
            return true;
        }
        //</editor-fold>

        //<editor-fold desc="[A]">
        //resets the entire game (ends it if not ended yet)
        if(sender.isOp() && args[0].equalsIgnoreCase("reset")){
            JuggernautGame.getInstance().reset();
            sender.sendMessage(prefix + "Game Reset!");
            return true;
        }
        //initialises the game (including map creation)
        if(sender.isOp() && args[0].equalsIgnoreCase("init")){
            JuggernautGame.getInstance().initialise();
            sender.sendMessage(prefix + "Initialised the game!");
            return true;
        }
        //stars the game if enough players are in.
        if(args[0].equalsIgnoreCase("start")){
            JuggernautGame.getInstance().start();
            sender.sendMessage("Starting game!");
            return true;
        }
        //forces the reset of the given player, in case something went wrong during game and they did not get reset properly.
        if(sender.isOp() && args[0].equalsIgnoreCase("forcereset")){
            if(args.length < 2){
                sender.sendMessage(prefix + "Usage: /jugg forcereset <player>");
                return true;
            }
            Player p2 = Bukkit.getPlayer(args[1]);
            if(p2!=null){
                new JuggernautPlayer(p2).destroy();
                sender.sendMessage(prefix + "Destroyed player!");
            }else{
                sender.sendMessage(prefix + "Couldnt find that player!");
            }
            return true;
        }
        //</editor-fold>


        //from here, all commands need to be done by a player.
        if(!(sender instanceof Player)){
            sender.sendMessage(prefix + "You must be a player to activate these commands!");
            return true;
        }
        Player p = (Player) sender;



        //<editor-fold desc="[A -T][testing commands]">
        //shoots a firework in the air, only visible to you. PEW!
        if(args[0].equalsIgnoreCase("firework") && p.isOp()){
            ItemStack fw = new ItemStack(Material.FIREWORK, 1);
            FireworkMeta meta = (FireworkMeta) fw.getItemMeta();

            meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.LIME).build());
            meta.setPower(1);
            fw.setItemMeta(meta);
            PacketHelper.displayFirework(p, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), CraftItemStack.asNMSCopy(fw));
            return true;
        }
        //</editor-fold>

        //<editor-fold desc="[GAME COMMANDS]">
        //joins the game, or forces someone else to join the game.
        if(args[0].equalsIgnoreCase("join")){
            if(args.length > 1){
                //wildcard, if inserted, join everyone on the server
                if(args[1].equalsIgnoreCase("*")){
                    for(Player onlinePlayer: Bukkit.getOnlinePlayers()){
                        onlinePlayer.sendMessage("[Jugg] You joined the game!");
                        JuggernautGame.getInstance().addPlayer(onlinePlayer);
                    }
                    return true;
                }
                //a force was detected, lets force join that player.
                Player p2 = Bukkit.getPlayer(args[1]);
                if(p2==null){
                    p.sendMessage("This player isnt online!");
                    return true;
                }
                JuggernautGame.getInstance().addPlayer(p2);
                p2.sendMessage("[Jugg] You joined the game!");
                return true;
            }
            //no further arguments, the sender joins the game
            if(JuggernautGame.getInstance().addPlayer(p)) {
                p.sendMessage("[Jugg] You joined the game!");
            }else{
                p.sendMessage("[Jugg] Something went wrong. Either you've already joined, or the game already started!");
            }
            return true;
        }
        //joins the game as a spectator. Not available when no game is active.
        if(args[0].equalsIgnoreCase("spec")){
            JuggernautSpectator JP = new JuggernautSpectator(p);
            JuggernautGame.getInstance().addSpectator(JP);
            return true;
        }
        //puts you in the lobby
        if(args[0].equalsIgnoreCase("lobby")){
            p.teleport(JuggernautGame.lobbySpawn);
            return true;
        }
        //</editor-fold>

        //send help message
        sender.sendMessage(prefix + "Help unavailable atm.");
        return true;
    }
}

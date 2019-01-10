package com.skyerzz.juggernaut;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Helps with sending packets so you wont have to break your head over them every time.
 * Created by sky on 5-9-2018.
 */
public class PacketHelper {

    /** when set to true, this will not hide any armorstands */
    private static final boolean showArmorstandsDebug = false; //false = hide, true == show

    /**
     * Sends a packet to show a single Redstonecolor particle to a player
     * @param p Player to send the packet to
     * @param x x location of the particle
     * @param y y location of the particle
     * @param z z location of the particle
     * @param color ParticleColor of the particle
     */
    public static void displaySingleParticle(Player p, float x, float y, float z, ParticleColor color){
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, x, y, z, color.getRed(), color.getGreen(), color.getBlue(), 1, 0, 0);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Sends a packet to show an armorstand with custom name, as a hologram, to a player.
     * @param p Player to send the packet to
     * @param x x location of the particle
     * @param y y location of the particle
     * @param z z location of the particle
     * @param text String to display on the hologram
     */
    public static EntityArmorStand displayHologram(Player p, double x, double y, double z, String text){
        //get a new armorstand
        EntityArmorStand stand = new EntityArmorStand(((CraftWorld) p.getPlayer().getWorld()).getHandle());

        stand.setLocation(x, y, z, 0,0); //xyz pitch yaw, we dont need the latter 2 for holograms.
        //set the custom text, and make it visible
        stand.setCustomName(text);
        stand.setCustomNameVisible(true);

        //stop the armorstand from falling down
        stand.setGravity(false);

        //if debug is off, turn it invisible
        stand.setInvisible(!showArmorstandsDebug);

        //set it small so its easier to control the height
        stand.setSmall(true);

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

        return stand;
    }

    /**
     * Send a chatMessage to a player
     * @param toSend Player to send the message to
     * @param chatType Type of chat we want to send it to
     * @param message Message to send
     */
    public static void sendChatMessage(Player toSend, byte chatType, String message){
        ChatComponentText chat = new ChatComponentText(message);

        PacketPlayOutChat packet = new PacketPlayOutChat(chat, chatType);
        ((CraftPlayer) toSend).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Sets a name on top of a player for another player, which will remove the actual name of the player.
     * @param toSend Player to send the packet to
     * @param toDisable Player to have the tooltip name above
     * @param tip String of the tooltip. If Null, no tooltip is set, but the name of the toDisable player will be removed fully.
     * @return EntityArmorStand used on player toDisable
     */
    public static EntityArmorStand setToolTipName(Player toSend, Player toDisable, @Nullable String tip){
        EntityArmorStand stand = new EntityArmorStand(((CraftWorld) toDisable.getPlayer().getWorld()).getHandle());

        //set the tooltip, if its set
        if(tip!=null){
            stand.setCustomName(tip);
            stand.setCustomNameVisible(true);
        }

        //make it look pretty and not fall too much
        stand.setGravity(false);
        stand.setInvisible(!showArmorstandsDebug);
        stand.setSmall(true);

        //send the armorstand
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        ((CraftPlayer) toSend).getHandle().playerConnection.sendPacket(packet);

        //make the armorstand attached to the player
        PacketPlayOutAttachEntity pckt2 = new PacketPlayOutAttachEntity(0, stand, ((CraftPlayer) toDisable).getHandle());
        ((CraftPlayer) toSend).getHandle().playerConnection.sendPacket(pckt2);

        return stand;

    }

    /**
     * Displays a Rotating item, suspended in midair (by armorstand)
     * @param p Player to send the packet to
     * @param x x Location
     * @param y y Location
     * @param z z Location
     * @param itemStack ItemStack to display
     * @return ArrayList of Entity used to create the item
     */
    public static ArrayList<Entity> displaySuspendedRotatingItem(Player p, double x, double y, double z, ItemStack itemStack){
        ArrayList<Entity> list = new ArrayList<>();

        //put an armorstand at the location first, and make it look pretty.
        EntityArmorStand stand = new EntityArmorStand(((CraftWorld) p.getPlayer().getWorld()).getHandle());

        stand.setLocation(x, y, z, 0,0);
        stand.setGravity(false);
        stand.setInvisible(!showArmorstandsDebug);
        stand.setSmall(true);

        //create an item entity from our itemstack
        EntityItem item = new EntityItem(((CraftWorld) p.getPlayer().getWorld()).getHandle());
        item.setLocation(x, y, z, 0f, 0f);
        item.setItemStack(itemStack);
        item.mount(stand);
        item.inactiveTick();

        //make the item ride our armorstand
        stand.getBukkitEntity().setPassenger(item.getBukkitEntity());

        //spawn the stand for the player
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

        //spawn the item for the player (including metadata)
        PacketPlayOutSpawnEntity itemPacket = new PacketPlayOutSpawnEntity(item, 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(itemPacket);
        PacketPlayOutEntityMetadata itemMeta = new PacketPlayOutEntityMetadata(item.getId(), item.getDataWatcher(), true);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(itemMeta);

        //tell the player the armorstand is ridden by the item
        PacketPlayOutAttachEntity attachEntity = new PacketPlayOutAttachEntity(0, item, stand);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(attachEntity);

        //return all entities used
        list.add(item);
        list.add(stand);
        return list;
    }

    /**
     * Displays a firework to the given player
     * @param p Player to show the firework
     * @param x x location (start)
     * @param y y location (start)
     * @param z z location (start)
     * @param firework ItemStack of the firework item activated
     */
    public static void displayFirework(Player p, double x, double y, double z, ItemStack firework){

        //create and spawn a firework rocket from our itemstack
        EntityFireworks fw = new EntityFireworks(((CraftWorld) p.getWorld()).getHandle());
        fw.setLocation(x, y, z, 0f, 0f);

        PacketPlayOutSpawnEntity alive = new PacketPlayOutSpawnEntity(fw, 76);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(alive);

        //i looked at what mincreaft does in vanilla whe you launch a firework on a server. I just copied what they do here by editing a datawatcher. dont ask me how or why, but it works.
        DataWatcher dataWatcher = fw.getDataWatcher();
        for(DataWatcher.WatchableObject obj: dataWatcher.c()){
            if(obj.c()==5 && obj.a()==8 && obj.b()==null){
                obj.a(firework);
            }
        }

        //give the entity some meta
        PacketPlayOutEntityMetadata itemMeta = new PacketPlayOutEntityMetadata(fw.getId(), fw.getDataWatcher(), true);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(itemMeta);

        //also play a sound
        PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect("fireworks.launch", x, y, z, 3f, 1f);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(sound);


        //wait 30 ticks before we detonate it.
        //IMPORTANT: wait too long and the players client will freeze!
        new BukkitRunnable() {
            @Override
            public void run() {
                //detonate the firework, and destroy the entity.
                PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fw, (byte) 17);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(status);
                PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fw.getId());
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroy);
            }
        }.runTaskLater(Juggernaut.pluginInstance, 30L);
    }

    /**
     * Updates a hologram for a player
     * @param p Player to update for
     * @param stand ArmorStand to update
     * @param newName new hologram string for the armorstand
     */
    public static void updateHologram(Player p, EntityArmorStand stand, String newName){
        if(stand==null || p==null){
            return; //something definately went wrong. safeguard
        }

        //create a new metadata packet with the correct datawatchers.
        DataWatcher dataWatcher = new DataWatcher(null);
        if(newName!=null) {
            dataWatcher.a(2, newName); //2 = optChat with custom name (see https://wiki.vg/Entity_metadata#Entity_Metadata_Format)
            dataWatcher.a(3, (byte) 1); //3 = customnamevisible , set to true.
        }else{
            dataWatcher.a(3, (byte) 0); //if name is null, simply set customnamevisible to false.
        }

        //send the packet to the player
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(stand.getId(), dataWatcher, true);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Despawns an entity given for the player.
     * Simply passes it along to despawnEntity(Player, int) with the entityID
     * @param p Player to despawn for
     * @param entity Entity to despawn
     */
    public static void despawnEntity(Player p, Entity entity){
        despawnEntity(p, entity.getId());
    }

    /**
     * Despawns an entity given for the player
     * @param p Player to despawn for
     * @param entityID ID of the entity to despawn
     */
    public static void despawnEntity(Player p, int entityID){
        PacketPlayOutEntityDestroy deadEntity = new PacketPlayOutEntityDestroy(entityID);
        ((CraftPlayer) p.getPlayer()).getHandle().playerConnection.sendPacket(deadEntity);

    }

    /**
     * Sets the status of a chest (opened/closed) for a player
     * @param p Player to set the status for
     * @param loc Location of the chest
     * @param open Boolean true if its opened, false if its closed.
     */
    public static void setChestStatus(Player p, Location loc, boolean open){
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(pos, Blocks.CHEST, 1, open ? 1 : 0);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Plays a sound to the player
     * @param p Player to play the sound to
     * @param loc Location of the source of the sound
     * @param sound Sound to play
     * @param pitch Pitch of the sound
     * @param volume Volume of the sound
     */
    public static void playSound(Player p, Location loc, Sound sound, float pitch, float volume){
        p.playSound(loc, sound, volume, pitch);
    }

    /**
     * Shows a particle(Type) to the player
     * @param p Player to show
     * @param loc Location of the particle
     * @param particle EnumParticle to spawn
     * @param amount amount to spawn
     */
    public static void showParticle(Player p, Location loc, EnumParticle particle, int amount){
        PacketPlayOutWorldParticles pckt = new PacketPlayOutWorldParticles(particle, false, (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0f, 0f, 0f, /*speed*/0f, amount);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pckt);
    }
}

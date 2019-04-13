package io.github.yehan2002.CombatLoggerPlus;

import io.github.yehan2002.CombatLoggerPlus.EventListener.RespawnListener;
import io.github.yehan2002.CombatLoggerPlus.Util.Config;
import io.github.yehan2002.CombatLoggerPlus.Util.DeathInventory;
import io.github.yehan2002.CombatLoggerPlus.Util.Util;
import io.github.yehan2002.CombatLoggerPlus.nms.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DeathMessages {
    private ConcurrentHashMap<UUID, Entity> LastDamager = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Integer> LastDamageTimer = new ConcurrentHashMap<>();
    private Config messages = Main.getMessages();
    private Random random = new Random();
    private boolean enableDeathInventory;
    private DeathInventory deathInventory = Main.getDeathInventory();

    DeathMessages(boolean enableDeathInventory){
        this.enableDeathInventory = enableDeathInventory;
    }

    public void addPlayer(UUID p, Entity l){
        LastDamager.put(p, l);
        LastDamageTimer.put(p, 20);
    }

    private String getMessage(String path) {
        List<String> stringList = messages.yml.getStringList(path);

        if (stringList.size() == 0)return  null;

        if (stringList.size() == 1) return stringList.get(0);

        return stringList.get(random.nextInt(stringList.size()));
    }

    private String getExplosion(Player p, DamageCause dc){
        String path;
        Entity lastDamager = LastDamager.get(p.getUniqueId());

        if (lastDamager instanceof TNTPrimed) {
            Entity primer = ((TNTPrimed) lastDamager).getSource();
            if (primer == null) {
                path = "explosion.tnt.default";

            } else if (primer == p) {
                path = "explosion.tnt.self";

            } else {
                path = "explosion.tnt.other";
            }
        }else if(lastDamager instanceof Firework){
            path = "explosion.firework";

        }else if(lastDamager instanceof Creeper){

            if (((Creeper) lastDamager).isPowered()){

                path = "mob.Creeper.charged";

            } else {

                path = "mob.Creeper.normal";
            }

        } else if(lastDamager instanceof EnderCrystal) {

            path = "explosion.enderCrystal";

        } else if (lastDamager instanceof WitherSkull){

            path = "projectile.witherSkull";

        } else {
            path ="normal." + dc.toString();
        }

        return path;
    }

    private String getProjectile(Player p, Entity lastDamager){
        String path = "projectile.default";
        Projectile pr = (Projectile) lastDamager;

        if (pr.getShooter() instanceof Skeleton) path = "mob.Skeleton";
        if (pr.getShooter() instanceof Stray) path = "mob.Stray";
        if (pr.getShooter() instanceof Llama) path = "mob.Llama";
        if (pr instanceof ShulkerBullet) path = "mob.Shulker";

        if (pr.getShooter() == p) path = "projectile.self";

        if (lastDamager instanceof WitherSkull){
            path = "mob.Wither";

        } else if (lastDamager instanceof SmallFireball || lastDamager instanceof LargeFireball){
            path = "projectile.fireball.default";
            if (pr.getShooter() != null && pr.getShooter() != p) path = "projectile.fireball.player";
            if (pr.getShooter() instanceof Ghast) path = "mob.Ghast";
            if (pr.getShooter() == p) path = "projectile.fireball.self";
        }



        return path;
    }


    public String playerDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        String path = null, message = null;
        DamageCause dc = e.getEntity().getLastDamageCause().getCause();
        Entity lastDamager = LastDamager.getOrDefault(p.getUniqueId(), null);


        // CHECKED
       if (dc == DamageCause.ENTITY_EXPLOSION){
           path = this.getExplosion(p, dc);
       }

       //CHECKED
       if(dc == DamageCause.THORNS) {
           path = "pvp.THORNS";
       }


       if(dc == DamageCause.FALLING_BLOCK) {
           path = "normal.FALLING_BLOCK";
           if (((FallingBlock) lastDamager).getMaterial() == Material.ANVIL) path = "normal.ANVIL";
       }

       if (dc == DamageCause.PROJECTILE) {
           path = this.getProjectile(p, lastDamager);
           if (Util.getEntity(lastDamager) != null) lastDamager = Util.getEntity(lastDamager);

       }

       if (dc == DamageCause.FALL && lastDamager instanceof EnderPearl) {
           path = "projectile.enderPearl";

       }

       if (dc == DamageCause.MAGIC ){
           path = "magic.default";
           if (lastDamager instanceof SplashPotion){

               SplashPotion potion = (SplashPotion) lastDamager;
               if (potion.getShooter() != null && (potion.getShooter() instanceof Entity)){
                   lastDamager = (Entity) potion.getShooter();
                   path = "magic.attacked";
               }
           }

       }



       if (dc == DamageCause.ENTITY_ATTACK || dc == DamageCause.ENTITY_SWEEP_ATTACK) {

            if (lastDamager instanceof Player) {
                path = "pvp.player" ;

            } else if (lastDamager instanceof AreaEffectCloud ){
                Entity source = Util.getEntity(lastDamager);
                path = "magic.default";
                if ((source instanceof Monster || source instanceof Player) && source != p ){
                    path = "magic.attacked";
                    lastDamager = source;
                }
                if (source instanceof EnderDragon) path = "mob.dragonBreath";

            } else {
                path = "mob." + lastDamager.getName().replace(" ", "");
                message = this.getMessage(path);
                if (message == null){
                    path = "mob." + Util.toTitleCase(lastDamager.getType().toString().toLowerCase().replace("_",""));
                    message = this.getMessage(path);
                }
                if (lastDamager instanceof EvokerFangs){
                    path = "mob.Evoker";
                    lastDamager = ((EvokerFangs) lastDamager).getOwner();
                }
            }

       }

        if (!(Util.isHostile(lastDamager) && (!lastDamager.isDead() || lastDamager != p))){
            lastDamager = null;
        }

       if (path == null){
           path ="normal." + dc.toString();

           if (lastDamager != null) {
               path = "pvp." + dc.toString();
               message = this.getMessage(path);

           }

        }


       if (message == null){
           message = this.getMessage(path);
       }

        if (message == null){
            System.out.println("Could not find death message: " + path);
            return null;
        }

        return sendMessage(p,lastDamager,message, RespawnListener.fakeEvents.contains(e));

    }

    private String sendMessage(Player p, Entity lastDamager, String message, boolean fakeEvent){
        JSONMessage jsonMessage = JSONMessage.create();
        for (String s : message.split(" ")) {

            if (s.equalsIgnoreCase("%player%")){
                jsonMessage.then(Util.getPlayerName(p) + " ");

                if (enableDeathInventory) {
                    jsonMessage.runCommand(deathInventory.createInventory(p, fakeEvent));
                    jsonMessage.tooltip(ChatColor.GOLD + "Click to view inventory");
                }

            } else if (s.equalsIgnoreCase("%attacker%")) {
                if (lastDamager instanceof Player) {
                    jsonMessage.then(Util.getPlayerName((Player) lastDamager) + " ");

                    if (enableDeathInventory) {
                        jsonMessage.runCommand(deathInventory.createInventory((Player) lastDamager, false));
                        jsonMessage.tooltip(ChatColor.GOLD + "Click to view inventory");
                    }

                } else {
                    jsonMessage.then(lastDamager.getName() + " ");

                }
            } else if (s.equalsIgnoreCase("%weapon%")){
                if (lastDamager instanceof LivingEntity){
                    //System.out.println(((LivingEntity) lastDamager).getEquipment().getItemInMainHand());
                }
            } else {
                jsonMessage.then(s + " ");
                jsonMessage.color(ChatColor.YELLOW);
            }
        }
        jsonMessage.send(p);
        message = message.replace("%player%", p.getName());
        if (lastDamager != null) message = message.replace("%attacker%", lastDamager.getName());
        return message;
    }


    void Tick(){
        for (Map.Entry<UUID, Integer> entry : LastDamageTimer.entrySet()) {

            if (entry.getValue() != 1) {
                LastDamageTimer.put(entry.getKey(), entry.getValue() - 1);

            } else {
                this.LastDamageTimer.remove(entry.getKey());
                this.LastDamager.remove(entry.getKey());
            }
        }
    }
}

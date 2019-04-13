package io.github.yehan2002.CombatLoggerPlus;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Respawn {
    private String Title;
    private String subTitle;
    private JavaPlugin plugin;
    private BukkitScheduler  scheduler;
    private Boolean fancyRespawn;
    private int Timer;



    private HashMap<Player, respawningPlayer> respawningPlayers = new HashMap<>();
    private ConcurrentHashMap<Player, Integer> RespawnTimer = new ConcurrentHashMap<>();


    Respawn(boolean fancyRespawn, int timer, String title, String subTitle) {
        this.Title = ChatColor.translateAlternateColorCodes('&', title);
        this.subTitle = ChatColor.translateAlternateColorCodes('&', subTitle);
        plugin = Main.getInstance();
        scheduler = Main.getInstance().getServer().getScheduler();
        this.fancyRespawn = fancyRespawn;
        this.Timer = timer + 1;
    }

    /**
     * Creates particles at player location
     * this func is automatically called on death
     *
     * @param p The player
     */
    public void DeathParticles(Player p){
        Location l = p.getLocation();
        l.getWorld().playEffect(l, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        l.getWorld().playEffect(l.add(0, 1, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

    }

    private void respawn(Player p){
        p.setGameMode(respawningPlayers.get(p).gm);
        p.setFlySpeed(respawningPlayers.get(p).speed);
        respawningPlayers.remove(p);
    }


    public void RespawnPlayer(Player p, boolean respawn) {
        if (respawn) {
            p.spigot().respawn();
        } else {
            p.setHealth(20);
        }

        p.setVelocity(new Vector(0 ,0,0));
        p.setVelocity(new Vector(0 ,0,0));

        if (fancyRespawn){
            if (respawningPlayers.containsKey(p)) return;
            respawningPlayers.put(p, new respawningPlayer(p.getFlySpeed(), p.getGameMode()));
            RespawnTimer.put(p, Timer);
            p.setGameMode(GameMode.SPECTATOR);
            p.setFlySpeed(0);
            p.setFlying(false);
        }

    }

    private class respawningPlayer{
        float speed;
        GameMode gm;
        respawningPlayer(float speed, GameMode gm){
            this.speed = speed;
            this.gm = gm;
        }
    }

    void onDisable(){
        for (Map.Entry<Player, respawningPlayer> entry: respawningPlayers.entrySet()){
            Player p = entry.getKey();
            p.setGameMode(entry.getValue().gm);
            p.setFlySpeed(entry.getValue().speed);
        }
    }

    void Tick(){
        for (Map.Entry<Player, Integer> entry :RespawnTimer.entrySet()) {
            Player p = entry.getKey();
            int value = entry.getValue();
            if (entry.getValue() != 1) {
                RespawnTimer.put(p, value - 1);
                p.sendTitle(Title, String.format(subTitle,value - 1), 1, 30, 20);
            } else {
                scheduler.runTaskLater(plugin, () -> this.respawn(p), 1);
                RespawnTimer.remove(p);
            }
        }
    }

}

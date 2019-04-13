package io.github.yehan2002.CombatLoggerPlus;

import io.github.yehan2002.CombatLoggerPlus.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CombatTags {
    private ConcurrentHashMap<Player, Double> CombatTagTimer = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Player, BossBar> BossBars = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Player, Boolean> mobTag = new ConcurrentHashMap<>();

    private double PlayerTag;
    private double MobTag;
    private String combatStart;
    private String combatEnd;
    private boolean enableBossBar;
    private String bossBarTitle;


    CombatTags(int mobTag, int playerTag, String combatStart, String combatEnd, boolean enableBossBar, String bossBarTitle) {

        this.MobTag = mobTag;
        this.PlayerTag = playerTag;
        this.combatStart = ChatColor.translateAlternateColorCodes('&', combatStart);
        this.combatEnd = ChatColor.translateAlternateColorCodes('&',combatEnd);
        this.enableBossBar = enableBossBar;
        this.bossBarTitle = ChatColor.translateAlternateColorCodes('&',bossBarTitle);
    }


    /**
     *  Adds the combat tag to the player
     * @param p the player
     * @param withMob if the event was triggered by a mob
     */
    // TODO fix potions
    @SuppressWarnings("WeakerAccess")
    void addPlayer(Player p, Boolean withMob){

        if (withMob) {
            CombatTagTimer.put(p, MobTag);
            mobTag.put(p, true);
        } else {
            CombatTagTimer.put(p,PlayerTag);
            mobTag.put(p, false);
        }

        if (!BossBars.containsKey(p) && enableBossBar) {
            BossBars.put(p, Bukkit.getServer().createBossBar(bossBarTitle, BarColor.RED, BarStyle.SOLID));
            BossBars.get(p).addPlayer(p);
            p.sendMessage(combatStart);

        } else if (!BossBars.get(p).isVisible()){
            BossBars.get(p).setVisible(true);
            p.sendMessage(combatStart);
        }


    }

    /**
     *  Removes the combat tag to the player
     * @param p the player
     */
    public void removePlayer(Player p){
        CombatTagTimer.remove(p);

        if (BossBars.containsKey(p)) {
            BossBars.get(p).setVisible(false);
            p.sendMessage(combatEnd);
        }
    }

    /**
     *  Checks if player has the combat tag
     * @param p the player
     * @return true if player has the combat tag else false
     */
    public boolean inCombat(Player p){
        return CombatTagTimer.containsKey(p);
    }

    void onDisable(){

        for (Map.Entry<Player, BossBar> entry : BossBars.entrySet()) {
            entry.getValue().setVisible(false);
        }

    }

    public void PlayerDamagePlayer(EntityDamageByEntityEvent e){
        this.addPlayer((Player) e.getEntity(), false);
        this.addPlayer((Player) e.getDamager(), false);
    }

    public void PlayerDamagePlayer(ProjectileHitEvent e){
        this.addPlayer((Player) e.getEntity().getShooter(), false);
        this.addPlayer((Player) e.getHitEntity(), false);
    }

    public void PlayerDamageByMob(ProjectileHitEvent e){

        if (e.getEntity().getShooter() instanceof Player ){
            if (Util.isHostile(e.getHitEntity())){
                this.addPlayer((Player) e.getEntity().getShooter(), true);
            }

        } else if (e.getHitEntity() instanceof Player){
            if (Util.isHostile(e.getHitEntity())) {
                this.addPlayer((Player) e.getHitEntity(), true);
            }
        }

    }

    public void PlayerDamageByMob(EntityDamageByEntityEvent e){

        if (e.getDamager() instanceof Player ){

            if (Util.isHostile(e.getEntity())){
                this.addPlayer((Player) e.getDamager(), true);
            }

        } else if (e.getEntity() instanceof Player){

            if (Util.isHostile(e.getEntity())){
                this.addPlayer((Player)e.getEntity(), true);
            }
        }

    }

    void Tick(){
        for (Map.Entry<Player, Double> entry : CombatTagTimer.entrySet()) {

            if (entry.getValue() != 1) {
                CombatTagTimer.put(entry.getKey(), entry.getValue() - 1);

                if (mobTag.getOrDefault(entry.getKey(), false)) {
                    BossBars.get(entry.getKey()).setProgress((entry.getValue() - 1) / MobTag);
                } else {
                    BossBars.get(entry.getKey()).setProgress((entry.getValue() - 1) / PlayerTag);

                }

            } else {
                this.removePlayer(entry.getKey());
            }
        }
    }


}

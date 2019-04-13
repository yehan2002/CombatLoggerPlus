package io.github.yehan2002.CombatLoggerPlus.EventListener;

import io.github.yehan2002.CombatLoggerPlus.CombatTags;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatTagListener implements Listener {

    private boolean mobsTriggerTag;
    private CombatTags combatTags;


    public CombatTagListener(CombatTags combatTags, boolean mobsTriggerTag) {
        this.mobsTriggerTag = mobsTriggerTag;
        this.combatTags = combatTags;
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttacked(EntityDamageByEntityEvent e) {

        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            combatTags.PlayerDamagePlayer(e);

        } else if (mobsTriggerTag && (e.getDamager() instanceof Player || e.getEntity() instanceof Player)) {
            combatTags.PlayerDamageByMob(e);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrow(ProjectileHitEvent e){
        if (e.getEntity() instanceof EnderPearl){
            return;
        }
        if (e.getHitEntity() == e.getEntity().getShooter()) return;

        if (e.getHitEntity() instanceof Player && e.getEntity().getShooter() instanceof Player){
            combatTags.PlayerDamagePlayer(e);
        } else if (e.getHitEntity() instanceof Player || e.getEntity().getShooter() instanceof Player){
            combatTags.PlayerDamageByMob(e);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e){
        if (combatTags.inCombat(e.getPlayer())) {
            e.getPlayer().setHealth(0);
            EntityDamageEvent ed = e.getPlayer().getLastDamageCause();
            if (ed != null) ed.setDamage(ed.getDamage() * 2);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e){
        combatTags.removePlayer(e.getEntity());
    }

}

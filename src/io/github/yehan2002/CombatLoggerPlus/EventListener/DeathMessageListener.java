package io.github.yehan2002.CombatLoggerPlus.EventListener;

import io.github.yehan2002.CombatLoggerPlus.DeathMessages;
import io.github.yehan2002.CombatLoggerPlus.Main;
import io.github.yehan2002.CombatLoggerPlus.Util.DeathInventory;
import io.github.yehan2002.CombatLoggerPlus.Util.Debug;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class DeathMessageListener implements Listener {

    private DeathMessages deathMessages = Main.getDeathMessages();
    private DeathInventory deathInventory = Main.getDeathInventory();
    private BukkitScheduler scheduler = Main.getInstance().getServer().getScheduler();

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e){
        String message = deathMessages.playerDeath(e);

        try {
            e.setDeathMessage("");
        } catch (Exception err){
            err.printStackTrace();
        }
        if (message != null) System.out.println(message);
    }


    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAttacked(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player){
            Debug.trace();
            Debug.info("DamageLogger", e.getDamager());
            deathMessages.addPlayer(e.getEntity().getUniqueId(), e.getDamager());
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e){
        if (e.getMessage().startsWith("deathmessages:openinv ")) {
            Player p = e.getPlayer();
            String uuid = e.getMessage().replace("deathmessages:openinv ", "");

            try {
                if (e.isAsynchronous()){
                    scheduler.runTaskLater(Main.getInstance(),() -> deathInventory.open(p, uuid),1);
                } else {
                    deathInventory.open(p, uuid);
                }
            } catch (ArrayIndexOutOfBoundsException ee){
                e.getPlayer().sendMessage(ChatColor.RED + "That inventory is no longer available.");
                ee.printStackTrace();
                e.setCancelled(true);

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onChat2(AsyncPlayerChatEvent e){
        if (e.getMessage().startsWith("deathmessages:openinv ")) e.setCancelled(true);
    }


}

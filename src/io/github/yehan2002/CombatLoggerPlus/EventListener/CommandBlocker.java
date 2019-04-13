package io.github.yehan2002.CombatLoggerPlus.EventListener;

import io.github.yehan2002.CombatLoggerPlus.CombatTags;
import io.github.yehan2002.CombatLoggerPlus.Main;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandBlocker implements Listener {
    private List<String> blockedCommands;
    private CombatTags combatTags = Main.getCombatTags();
    private String commandBlockedMessage ;

    public CommandBlocker(List<String> blockedCommands, String commandBlockedMessage){
        this.blockedCommands = blockedCommands;
        this.commandBlockedMessage = ChatColor.translateAlternateColorCodes('&',commandBlockedMessage);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e){
        if (combatTags.inCombat(e.getPlayer())) {
            if (blockedCommands.contains(e.getMessage().substring(1))) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(commandBlockedMessage);
            }
        }
    }
}

package io.github.yehan2002.CombatLoggerPlus.Util;

import io.github.yehan2002.CombatLoggerPlus.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;
import java.util.Objects;

public class Debug implements Listener {
    private static boolean Debug = false;
    private static boolean Dev = false;
    private static JavaPlugin plugin;

    public static void enable(JavaPlugin pl, boolean debug, boolean dev){
        Debug = debug;
        Dev = dev || Paths.get(pl.getDataFolder().getAbsolutePath(), "dev.lock").toFile().exists();
        plugin = pl;
        pl.getServer().getPluginManager().registerEvents(new Debug(), pl);
    }

    public static void debug(Object message){
        Print(ChatColor.GOLD, "LOG", "" , message);
    }

    public static void info(String prepend, Object message){
        Print(ChatColor.YELLOW, "INFO", prepend,message);
    }

    public static void warn(String prepend, Object message){
        Print(ChatColor.RED,"WARN",prepend,message);
    }

    public static void trace(){
        if (!Debug) return;
        RuntimeException exception = new RuntimeException();
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(
                plugin,
                () -> Print(ChatColor.GOLD, "Trace", "", exception.getStackTrace()[1]),
                1); // https://stackoverflow.com/a/26122232
    }


    public static void error(String prepend, Object message){
        Print(ChatColor.DARK_RED, "ERROR", prepend,message);
    }

    private static void Print(ChatColor color, String level, String prepend, Object message){
        if (!Debug) return;

        if (!prepend.isEmpty()) prepend = prepend + ":";
        Bukkit.getConsoleSender().sendMessage(stripColor(color + String.format("[%s]%s %s", level, prepend, Objects.toString(message))));

    }

    private static String stripColor(String s){
        if (Dev) return s;
        return String.format("[%s]%s", plugin.getName(), ChatColor.stripColor(s));
    }

    private Debug(){
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent e){
        if (e.getMessage().toLowerCase().startsWith("/pldebug")){
            if (!e.getPlayer().isOp() || !e.getPlayer().hasPermission("pluginDebugger.use")){
                e.setCancelled(true);
                return;
            }
            if (e.getMessage().toLowerCase().contains("/pldebug " + plugin.getName().toLowerCase())) {
                e.setCancelled(true);
                String[] command = e.getMessage().split(" ");
                if (command.length == 3) {
                    if (command[2].equalsIgnoreCase("on") || command[2].equalsIgnoreCase("true")) {
                        Debug = true;
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Enabled debugging for " + plugin.getName());
                        return;
                    } else if (command[2].equalsIgnoreCase("off") || command[2].equalsIgnoreCase("false")) {
                        Debug = false;
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Disabled debugging for " + plugin.getName());
                        return;
                    }
                }
                e.getPlayer().sendMessage("Usage: /pldebug <plugin> <on|off>");
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCommand2(PlayerCommandPreprocessEvent e){
        if (e.getMessage().toLowerCase().contains("/pldebug")){
            String[] command = e.getMessage().split(" ");
            if (command.length == 3){
                e.getPlayer().sendMessage(ChatColor.RED + "Unknown plugin \"" + command[1] + "\"");
            } else {
                e.getPlayer().sendMessage("Usage: /pldebug <plugin> <on|off>");
            }
            System.out.println("unhandled");
            e.setCancelled(true);
        }
    }
}

package io.github.yehan2002.CombatLoggerPlus.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class PlayerUtils {
    private static String version = "";

    private static Class CraftPlayer;
    private static Method getCraftPlayerHandler, getEntityPlayerCombatTracker, IChatBaseComponentToString, getCombatTrackerDeathMessage;

    static {
        try {
            String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            version = split[split.length - 1];
            CraftPlayer = getClass("{obc}.entity.CraftPlayer");
            Class combatTracker = getClass("{nms}.CombatTracker");
            Class IChatBaseComponent = getClass("{nms}.IChatBaseComponent");
            Class entityPlayer = getClass("{nms}.EntityPlayer");

            getCraftPlayerHandler = CraftPlayer.getMethod("getHandle");
            getEntityPlayerCombatTracker = entityPlayer.getMethod("getCombatTracker");
            IChatBaseComponentToString = IChatBaseComponent.getMethod("toPlainText");
            getCombatTrackerDeathMessage = combatTracker.getMethod("getDeathMessage");

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();

        }
    }

    private static Class<?> getClass(String path) throws ClassNotFoundException {

        return Class.forName(path.replace("{nms}", "net.minecraft.server." + version).replace("{obc}", "org.bukkit.craftbukkit." + version));
    }


    public static String getDeathMessage(Player p){
        try {

            Object pl = CraftPlayer.cast(p);
            Object ep = getCraftPlayerHandler.invoke(pl);
            Object ct = getEntityPlayerCombatTracker.invoke(ep);
            Object dm = getCombatTrackerDeathMessage.invoke(ct);
            return (String) IChatBaseComponentToString.invoke(dm);

        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            e.printStackTrace();
        }
        return p.getName() + " died.";
    }



}

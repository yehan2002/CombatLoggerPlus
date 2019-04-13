package io.github.yehan2002.CombatLoggerPlus.Util;

import org.bukkit.entity.*;

public class Util {
    public static Entity getEntity(Entity e){
        if (e instanceof AreaEffectCloud && ((AreaEffectCloud) e).getSource() instanceof Entity){
            return (Entity) ((AreaEffectCloud) e).getSource();
        } else if (e instanceof Projectile && ((Projectile) e).getShooter() instanceof Entity) {
            return (Entity) ((Projectile) e).getShooter();
        }
        return e;
    }

    public static boolean isHostile(Entity e){
        return  (e instanceof Monster || e instanceof Player || e instanceof Ghast ||
                e instanceof Llama  || e instanceof PolarBear ||
                e instanceof Shulker || e instanceof Wolf || e instanceof Slime);
    }

    public static String toTitleCase(String str) {
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < words.length; i++) {
            ret.append(Character.toUpperCase(words[i].charAt(0)));
            ret.append(words[i].substring(1));
            if(i < words.length - 1) {
                ret.append(' ');
            }
        }
        return ret.toString();
    }

    public static String getPlayerName(Player p){
        return p.getDisplayName();
    }


}

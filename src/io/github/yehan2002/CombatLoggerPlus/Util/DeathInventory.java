package io.github.yehan2002.CombatLoggerPlus.Util;

import io.github.yehan2002.CombatLoggerPlus.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeathInventory {

    private ConcurrentMap<String,IconMenu> inventory = new ConcurrentHashMap<>();
    private BukkitScheduler scheduler = Main.getInstance().getServer().getScheduler();
    public static ArrayList<UUID> opened = new ArrayList<>();
    private String message;

    public DeathInventory(String message){
        this.message = message;
    }

    /**
     * Creates a inventory on player death
     * @param p the Player
     * @param fakeEvent
     * @return String to open death message
     */
    public String createInventory(Player p, boolean fakeEvent) {

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(p);
        skull.setItemMeta(meta);

        ItemStack potions = new ItemStack(Material.POTION);

        PotionMeta pm = (PotionMeta) potions.getItemMeta();

        for (PotionEffect ap : p.getActivePotionEffects()) {
            pm.addCustomEffect(new PotionEffect(ap.getType(), ap.getDuration(), ap.getAmplifier()), true);
        }

        potions.setItemMeta(pm);


        IconMenu im = new IconMenu(p.getDisplayName() + "'s Inventory", 54, event -> event.setWillClose(false), Main.getInstance());
        double health =fakeEvent ? 0 : p.getHealth();
        im.fill(0, 9, new ItemStack(Material.STAINED_GLASS_PANE))
                .setOption(1, skull)
                .setOption(3, potions, ChatColor.GREEN + "Active Potion Effects")
                .setOption(5, new ItemStack(Material.SKULL_ITEM), ChatColor.RED + "Dead")
                .setOption(5, new ItemStack(Material.SPECKLED_MELON, (int) health), ChatColor.WHITE + "Health")
                .setOption(6, new ItemStack(Material.COOKED_BEEF, p.getFoodLevel()), ChatColor.WHITE + "Food")
                .setOption(7, new ItemStack(Material.EXP_BOTTLE), ChatColor.AQUA + "Exp: " + p.getLevel());

        im.fill(45, 54, new ItemStack(Material.STAINED_GLASS_PANE));


        for (int i = 0; i < 36; i++) {
            im.setOption(9 + i, p.getInventory().getContents()[i]);
        }

        for (int i = p.getInventory().getArmorContents().length - 1; i >= 0; i--) {
            im.setOption(46 + i, p.getInventory().getArmorContents()[i]);
        }

        im.setOption(51, p.getInventory().getItemInOffHand());

        String uuid = UUID.randomUUID().toString();
        while (inventory.containsKey(uuid)) uuid = UUID.randomUUID().toString();

        inventory.put(uuid, im);

        String Uuid = uuid;
        scheduler.runTaskLater(Main.getInstance(), () -> inventory.remove(Uuid), 1200);

        return String.format("deathmessages:openinv %s", uuid);
    }

    public void open(Player p, String uuid){
        if (!inventory.containsKey(uuid)){
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }
        inventory.get(uuid).open(p);
        opened.add(p.getUniqueId());
    }
}

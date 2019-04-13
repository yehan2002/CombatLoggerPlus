package io.github.yehan2002.CombatLoggerPlus.EventListener;

import io.github.yehan2002.CombatLoggerPlus.Main;
import io.github.yehan2002.CombatLoggerPlus.Respawn;
import io.github.yehan2002.CombatLoggerPlus.Util.Debug;
import io.github.yehan2002.CombatLoggerPlus.Util.ExpManager;
import io.github.yehan2002.CombatLoggerPlus.nms.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class RespawnListener implements Listener  {
    private BukkitScheduler scheduler = Main.getInstance().getServer().getScheduler();
    private Respawn respawn;
    public static ArrayList<PlayerDeathEvent> fakeEvents = new ArrayList<>();
    private HashMap<UUID, Long> ignoreTime = new HashMap<UUID, Long>();

    public RespawnListener(Respawn respawn){
        this.respawn = respawn;
    }

    private List<UUID> ignoredPlayers = new LinkedList<>();

    @SuppressWarnings("unused")
    @EventHandler()
    public void onPlayerDeath(PlayerDeathEvent e){

        if (ignoredPlayers.contains(e.getEntity().getUniqueId())){
            return;
        }

        Player p = e.getEntity();

        respawn.DeathParticles(e.getEntity());

        scheduler.runTaskLater(Main.getInstance(), () -> respawn.RespawnPlayer(e.getEntity(), true), 5 + new Random().nextInt(35));
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerDamage(EntityDamageEvent e){

        if (e.getEntity() instanceof Player && !e.getEntity().isDead() && ((Player) e.getEntity()).getHealth() != 0){
            Player p = (Player) e.getEntity();
            if (p.getHealth() - e.getFinalDamage() <= 0) {
                respawn.DeathParticles(p);
                scheduler.runTaskLater(Main.getInstance(), () -> createFakeDeathEvent(p), 1);
                if (p.getHealth() - 1 > 0) {
                    e.setDamage(1);
                } else {
                    e.setDamage(0);
                }
            }
        } else {
        }

    }

    private void createFakeDeathEvent(Player p){
        boolean dropInv = this.dropInventory(p);
        PlayerDeathEvent event = new PlayerDeathEvent(p, dropInv ? Arrays.asList(p.getInventory().getContents()) : new ArrayList<>(),  ExpManager.getExp(p), PlayerUtils.getDeathMessage(p));
        boolean ignoreEvent = true;
        if (!p.isDead() || p.getHealth() < 1) {
            p.spigot().respawn();
        }
        if (System.currentTimeMillis() - ignoreTime.getOrDefault(p.getUniqueId(), 0L) > 2000 ) {
            ignoreTime.put(p.getUniqueId(), System.currentTimeMillis());
            fakeEvents.add(event);
            ignoredPlayers.add(p.getUniqueId());

            Debug.debug("calling event since not in cool down");
            Debug.trace();

            try {
                Bukkit.getServer().getPluginManager().callEvent(event);

            } catch (Exception e) {
                e.printStackTrace();
                Debug.error("Error Calling Event", e.getMessage());
            }

            fakeEvents.remove(event);
            ignoredPlayers.remove(p.getUniqueId());
            ignoreEvent = false;

            if  (event.getDeathMessage() != null && !event.getDeathMessage().equals("")){
                Bukkit.getServer().broadcastMessage(event.getDeathMessage());
            }
        }

        Location l = p.getLocation();

        if (event.getDroppedExp() != 0){
            p.setExp(0);
            p.setLevel(0);
            ((ExperienceOrb)l.getWorld().spawnEntity(l, EntityType.EXPERIENCE_ORB)).setExperience(event.getDroppedExp());

        }

        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());

        }

        if (dropInv) {
            for (ItemStack drop : event.getDrops()) {
                if (drop == null) continue;
                if (drop.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) continue;
                l.getWorld().dropItem(l, drop);
            }
            p.getInventory().clear();
        }
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(10);
        p.setNoDamageTicks(100);
        p.setFireTicks(0);
        p.setRemainingAir(20);


        if (p.getBedSpawnLocation() == null){
            Location location;
            try {
                location= getSafeLocation(l.getWorld().getSpawnLocation());
                if (location == null) location = l.getWorld().getSpawnLocation();
            } catch (StackOverflowError e){
                location = l.getWorld().getSpawnLocation();
            }
            p.teleport(location);
        } else {
            p.teleport(p.getBedSpawnLocation());
        }

        if(!ignoreEvent) respawn.RespawnPlayer(p, false);
    }

    private HashMap<World,Location> safeLocation = new HashMap<>();
    private Location getSafeLocation(Location l){
        if (safeLocation.containsKey(l.getWorld())){
            return safeLocation.get(l.getWorld());
        }

        if (l.getBlock().getType() == Material.AIR &&
                l.add(0,1,0).getBlock().getType() == Material.AIR){
            safeLocation.put(l.getWorld(),l);
            return l;
        }
         if (l.getBlockY() >= 255) return null;


        return this.getSafeLocation(l.add(0,1,0));

    }

    private boolean dropInventory(Player p){
        return !p.getWorld().isGameRule("keepInventory") || p.getWorld().getGameRuleValue("keepInventory").equalsIgnoreCase("false");
    }
}

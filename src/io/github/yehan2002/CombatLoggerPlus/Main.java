package io.github.yehan2002.CombatLoggerPlus;

import io.github.yehan2002.CombatLoggerPlus.EventListener.CombatTagListener;
import io.github.yehan2002.CombatLoggerPlus.EventListener.CommandBlocker;
import io.github.yehan2002.CombatLoggerPlus.EventListener.DeathMessageListener;
import io.github.yehan2002.CombatLoggerPlus.EventListener.RespawnListener;
import io.github.yehan2002.CombatLoggerPlus.Util.Config;
import io.github.yehan2002.CombatLoggerPlus.Util.DeathInventory;
import io.github.yehan2002.CombatLoggerPlus.Util.Debug;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Main extends JavaPlugin {

    @Getter private static Main instance;
    @Getter private static CombatTags combatTags;
    @Getter private static DeathMessages deathMessages;
    @Getter private static DeathInventory deathInventory;
    @Getter private static Respawn respawn;
    @Getter private static Config messages;
    @Getter private static Config configs;

    @Override
    public void onEnable() {
        //new AutoUpdater("24610", this).Update();
        Debug.enable(this, false, false);
        instance = this;
        configs = new Config("config.yml");
        messages = new Config("messages.yml");
        messages.saveDefault(true);
        configs.saveDefault(true);

        deathInventory = new DeathInventory(configs.getString("customDeathMessages.deathInventory.notAvailable"));

        if (configs.getBool("combatTag.enabled")) this.enableCombatTag();

        if (configs.getBool("autoRespawn.enabled")) this.enableRespawn();

        if (configs.getBool("combatTag.disableCommands.enabled")) this.enableCommandBlocker();

        if (configs.getBool("customDeathMessages.enabled")) enableDeathMessages();


    }

    @Override
    public void onDisable() {
        if (combatTags != null) combatTags.onDisable();
        if (respawn != null) respawn.onDisable();
        for (UUID uuid : DeathInventory.opened) {
            if (Bukkit.getPlayer(uuid) != null){
                Bukkit.getPlayer(uuid).closeInventory();
            }
        }
    }

    private void enableCombatTag(){
        combatTags = new CombatTags(
                configs.getInt("combatTag.time.mob"),
                configs.getInt("combatTag.time.player"),
                configs.getString("combatTag.startMessage"),
                configs.getString("combatTag.endMessage"),
                configs.getBool("combatTag.bossBar.enabled"),
                configs.getString("combatTag.bossBar.title"));

        this.getServer().getPluginManager().registerEvents(new CombatTagListener(combatTags, configs.getBool("combatTag.mobsTrigger")), this);

        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> combatTags.Tick(), 20,20 );

    }

    private void enableRespawn(){
        respawn = new Respawn(
                configs.getBool("autoRespawn.fancyRespawn.enabled"),
                configs.getInt("autoRespawn.fancyRespawn.time"),
                configs.getString("autoRespawn.fancyRespawn.title"),
                configs.getString("autoRespawn.fancyRespawn.subTitle"));

        this.getServer().getPluginManager().registerEvents(new RespawnListener(respawn), this);

        if (configs.getBool("autoRespawn.fancyRespawn.enabled")) {
            this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> respawn.Tick(), 20, 20);
        }


    }

    private void enableCommandBlocker(){
        this.getServer().getPluginManager().registerEvents(new CommandBlocker(
                configs.yml.getStringList("combatTag.disableCommands.disabledCommands"),
                configs.getString("combatTag.disableCommands.denyMessage")), this);

    }

    private void enableDeathMessages(){
        deathMessages = new DeathMessages(configs.getBool("customDeathMessages.deathInventory.enabled"));


        this.getServer().getPluginManager().registerEvents(new DeathMessageListener(), this);
        // tickers
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> deathMessages.Tick(), 20,20 );
    }


}

package com.miplugin.aldeanonetherite;

import com.miplugin.aldeanonetherite.command.AldeanoCommand;
import com.miplugin.aldeanonetherite.data.DataManager;
import com.miplugin.aldeanonetherite.listener.MenuListener;
import com.miplugin.aldeanonetherite.listener.PlayerListener;
import com.miplugin.aldeanonetherite.listener.ProgressListener;
import com.miplugin.aldeanonetherite.listener.VillagerListener;
import com.miplugin.aldeanonetherite.mission.MissionManager;
import com.miplugin.aldeanonetherite.mission.MissionRegistry;
import com.miplugin.aldeanonetherite.ui.BossBarManager;
import com.miplugin.aldeanonetherite.villager.VillagerManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AldeanoNetheritePlugin extends JavaPlugin {

    private MissionRegistry registry;
    private DataManager data;
    private BossBarManager bars;
    private VillagerManager villagers;
    private MissionManager missions;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registry = new MissionRegistry(this);
        registry.load();

        data = new DataManager(this);
        data.load();

        bars = new BossBarManager(this);
        villagers = new VillagerManager(this);
        missions = new MissionManager(this);

        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        getServer().getPluginManager().registerEvents(new ProgressListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        PluginCommand command = getCommand("aldeano");
        if (command != null) {
            AldeanoCommand executor = new AldeanoCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        // Guarda el progreso cada minuto si hubo cambios
        getServer().getScheduler().runTaskTimer(this, () -> data.saveIfDirty(), 1200L, 1200L);

        // Si el aldeano desapareció, se vuelve a crear en su ubicación guardada
        getServer().getScheduler().runTaskLater(this, () -> villagers.ensure(), 40L);

        // Por si el plugin se recarga con jugadores conectados
        for (Player player : getServer().getOnlinePlayers()) {
            bars.refresh(player);
        }

        getLogger().info("AldeanoNetherite activado.");
    }

    @Override
    public void onDisable() {
        if (data != null) {
            data.save();
        }
        if (bars != null) {
            bars.clearAll();
        }
    }

    public MissionRegistry registry() {
        return registry;
    }

    public DataManager data() {
        return data;
    }

    public BossBarManager bars() {
        return bars;
    }

    public VillagerManager villagers() {
        return villagers;
    }

    public MissionManager missions() {
        return missions;
    }
}

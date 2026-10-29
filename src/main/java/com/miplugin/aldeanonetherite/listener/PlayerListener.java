package com.miplugin.aldeanonetherite.listener;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Restaura la bossbar al entrar y limpia al salir. */
public final class PlayerListener implements Listener {

    private final AldeanoNetheritePlugin plugin;

    public PlayerListener(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.bars().refresh(player);
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.bars().forget(event.getPlayer());
        plugin.data().saveIfDirty();
    }
}

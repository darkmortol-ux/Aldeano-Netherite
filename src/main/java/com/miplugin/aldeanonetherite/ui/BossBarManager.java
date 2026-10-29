package com.miplugin.aldeanonetherite.ui;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import com.miplugin.aldeanonetherite.data.PlayerData;
import com.miplugin.aldeanonetherite.mission.Mission;
import com.miplugin.aldeanonetherite.util.Msg;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Muestra el progreso de la misión en una bossbar. */
public final class BossBarManager {

    private final AldeanoNetheritePlugin plugin;
    private final Map<UUID, BossBar> bars = new HashMap<>();

    public BossBarManager(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    /** Crea, actualiza u oculta la barra según el estado actual del jugador. */
    public void refresh(Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        Mission mission = plugin.registry().get(data.missionId);

        if (data.state == PlayerData.State.NONE || mission == null) {
            hide(player);
            return;
        }

        Component title;
        float progress;
        BossBar.Color color;

        if (data.state == PlayerData.State.COMPLETED) {
            title = Msg.c("&a&l✔ Misión completada &7- &fHabla con el aldeano");
            progress = 1f;
            color = BossBar.Color.GREEN;
        } else {
            title = Msg.c(mission.difficulty().chatColor() + "&l" + mission.name()
                    + " &7- &f" + data.progress + "/" + mission.amount());
            progress = Math.max(0f, Math.min(1f, (float) data.progress / mission.amount()));
            color = mission.difficulty().barColor();
        }

        BossBar bar = bars.get(player.getUniqueId());
        if (bar == null) {
            bar = BossBar.bossBar(title, progress, color, BossBar.Overlay.PROGRESS);
            bars.put(player.getUniqueId(), bar);
            player.showBossBar(bar);
        } else {
            bar.name(title);
            bar.progress(progress);
            bar.color(color);
        }
    }

    public void hide(Player player) {
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    /** Olvida la barra de un jugador que se desconecta. */
    public void forget(Player player) {
        bars.remove(player.getUniqueId());
    }

    public void clearAll() {
        for (Map.Entry<UUID, BossBar> entry : bars.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        bars.clear();
    }
}

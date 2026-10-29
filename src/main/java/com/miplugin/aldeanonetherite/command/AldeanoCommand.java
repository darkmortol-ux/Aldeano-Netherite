package com.miplugin.aldeanonetherite.command;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import com.miplugin.aldeanonetherite.data.PlayerData;
import com.miplugin.aldeanonetherite.mission.Mission;
import com.miplugin.aldeanonetherite.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Comando /aldeano. */
public final class AldeanoCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN = "aldeanonetherite.admin";

    private final AldeanoNetheritePlugin plugin;

    public AldeanoCommand(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("set")) {
            if (!sender.hasPermission(ADMIN)) {
                Msg.send(sender, "&cNo tienes permiso.");
                return true;
            }
            if (!(sender instanceof Player player)) {
                Msg.send(sender, "&cSolo un jugador puede usar este comando.");
                return true;
            }
            plugin.villagers().place(player);
            Msg.send(sender, "&aAldeano colocado en tu posición.");
            return true;
        }

        if (sub.equals("remove")) {
            if (!sender.hasPermission(ADMIN)) {
                Msg.send(sender, "&cNo tienes permiso.");
                return true;
            }
            plugin.villagers().remove();
            Msg.send(sender, "&aAldeano eliminado.");
            return true;
        }

        if (sub.equals("reset")) {
            if (!sender.hasPermission(ADMIN)) {
                Msg.send(sender, "&cNo tienes permiso.");
                return true;
            }
            if (args.length < 2) {
                Msg.send(sender, "&cUso: /aldeano reset <jugador>");
                return true;
            }
            OfflinePlayer target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                target = Bukkit.getOfflinePlayerIfCached(args[1]);
            }
            if (target == null) {
                Msg.send(sender, "&cNo encontré a ese jugador.");
                return true;
            }
            plugin.data().get(target.getUniqueId()).resetAll();
            plugin.data().markDirty();
            plugin.data().save();
            Player online = target.getPlayer();
            if (online != null) {
                plugin.bars().refresh(online);
            }
            Msg.send(sender, "&aSe reinició el progreso de &f" + args[1] + "&a.");
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission(ADMIN)) {
                Msg.send(sender, "&cNo tienes permiso.");
                return true;
            }
            plugin.reloadConfig();
            Msg.send(sender, "&aConfiguración recargada.");
            return true;
        }

        if (sub.equals("mision")) {
            if (!(sender instanceof Player player)) {
                Msg.send(sender, "&cSolo un jugador puede usar este comando.");
                return true;
            }
            PlayerData data = plugin.data().get(player.getUniqueId());
            Mission mission = plugin.registry().get(data.missionId);
            if (data.state == PlayerData.State.NONE || mission == null) {
                Msg.send(sender, "&7No tienes ninguna misión. Habla con el aldeano.");
            } else if (data.state == PlayerData.State.COMPLETED) {
                Msg.send(sender, "&aTu misión está completada. Habla con el aldeano para intercambiar.");
            } else {
                Msg.send(sender, "&6Misión: &f" + mission.name() + " &7- &e" + mission.description());
                Msg.send(sender, "&7Progreso: &f" + data.progress + "/" + mission.amount());
            }
            return true;
        }

        if (sub.equals("abandonar")) {
            if (!(sender instanceof Player player)) {
                Msg.send(sender, "&cSolo un jugador puede usar este comando.");
                return true;
            }
            if (plugin.missions().abandon(player)) {
                Msg.send(sender, "&eAbandonaste la misión. Habla con el aldeano para recibir otra.");
            } else {
                Msg.send(sender, "&cNo tienes una misión en curso que abandonar.");
            }
            return true;
        }

        help(sender);
        return true;
    }

    private void help(CommandSender sender) {
        Msg.send(sender, "&eComandos:");
        Msg.send(sender, "&f/aldeano mision &7- Ver tu misión actual");
        Msg.send(sender, "&f/aldeano abandonar &7- Abandonar la misión en curso");
        if (sender.hasPermission(ADMIN)) {
            Msg.send(sender, "&f/aldeano set &7- Colocar el aldeano donde estás");
            Msg.send(sender, "&f/aldeano remove &7- Quitar el aldeano");
            Msg.send(sender, "&f/aldeano reset <jugador> &7- Reiniciar el progreso de un jugador");
            Msg.send(sender, "&f/aldeano reload &7- Recargar la configuración");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("mision", "abandonar"));
            if (sender.hasPermission(ADMIN)) {
                subs.addAll(List.of("set", "remove", "reset", "reload"));
            }
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(s);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission(ADMIN)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}

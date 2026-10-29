package com.miplugin.aldeanonetherite.mission;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import com.miplugin.aldeanonetherite.data.Offer;
import com.miplugin.aldeanonetherite.data.PlayerData;
import com.miplugin.aldeanonetherite.ui.TradeMenu;
import com.miplugin.aldeanonetherite.util.Msg;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Lógica de asignar misiones, contar el progreso y generar las ofertas. */
public final class MissionManager {

    /** Pieza de diamante, su versión en netherite y los encantamientos posibles. */
    private record ItemSet(Material diamond, Material netherite, List<String> enchants) {
    }

    private static final List<ItemSet> ITEM_SETS = List.of(
            new ItemSet(Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
                    List.of("protection", "unbreaking", "respiration", "thorns", "blast_protection",
                            "projectile_protection", "fire_protection")),
            new ItemSet(Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
                    List.of("protection", "unbreaking", "thorns", "blast_protection",
                            "projectile_protection", "fire_protection")),
            new ItemSet(Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS,
                    List.of("protection", "unbreaking", "thorns", "blast_protection",
                            "projectile_protection", "fire_protection", "swift_sneak")),
            new ItemSet(Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS,
                    List.of("protection", "unbreaking", "feather_falling", "depth_strider",
                            "soul_speed", "thorns")),
            new ItemSet(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                    List.of("sharpness", "unbreaking", "looting", "fire_aspect", "knockback",
                            "sweeping_edge", "smite", "bane_of_arthropods")),
            new ItemSet(Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
                    List.of("efficiency", "unbreaking", "fortune")),
            new ItemSet(Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                    List.of("efficiency", "unbreaking", "sharpness")),
            new ItemSet(Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
                    List.of("efficiency", "unbreaking", "fortune")),
            new ItemSet(Material.DIAMOND_HOE, Material.NETHERITE_HOE,
                    List.of("efficiency", "unbreaking", "fortune"))
    );

    private final AldeanoNetheritePlugin plugin;

    public MissionManager(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------- interacción con el aldeano
    public void interact(Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        Mission mission = plugin.registry().get(data.missionId);

        // Estados dañados (por ejemplo, una misión que ya no existe)
        if (data.state == PlayerData.State.IN_PROGRESS && mission == null) {
            data.clearMission();
        }
        if (data.state == PlayerData.State.COMPLETED && data.offer == null) {
            data.clearMission();
        }

        switch (data.state) {
            case NONE -> assign(player, data);
            case IN_PROGRESS -> showStatus(player, data, mission);
            case COMPLETED -> TradeMenu.open(plugin, player);
        }
    }

    private void assign(Player player, PlayerData data) {
        List<Mission> candidates = new ArrayList<>();
        for (Mission m : plugin.registry().all()) {
            if (!data.completed.contains(m.id())) {
                candidates.add(m);
            }
        }
        if (candidates.isEmpty()) {
            // Ya completó las 50: se reinicia el historial para que pueda repetir
            data.completed.clear();
            candidates.addAll(plugin.registry().all());
        }
        if (candidates.isEmpty()) {
            Msg.send(player, "&cNo hay misiones disponibles. Avisa a un administrador.");
            return;
        }

        Mission mission = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        data.state = PlayerData.State.IN_PROGRESS;
        data.missionId = mission.id();
        data.progress = 0;
        data.offer = null;
        plugin.data().markDirty();
        plugin.data().save();

        Msg.send(player, "&6¡Nueva misión! &f" + mission.name()
                + " &7(Dificultad: " + mission.difficulty().chatColor() + mission.difficulty().label() + "&7)");
        Msg.send(player, "&e" + mission.description());
        Msg.send(player, "&7Tu progreso se muestra en la barra de arriba. Vuelve conmigo al terminar.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
        plugin.bars().refresh(player);
    }

    public void showStatus(Player player, PlayerData data, Mission mission) {
        Msg.send(player, "&6Misión actual: &f" + mission.name()
                + " &7(" + mission.difficulty().chatColor() + mission.difficulty().label() + "&7)");
        Msg.send(player, "&e" + mission.description());
        Msg.send(player, "&7Progreso: &f" + data.progress + "/" + mission.amount());
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        plugin.bars().refresh(player);
    }

    /** El jugador abandona la misión actual (solo si aún no la completó). */
    public boolean abandon(Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        if (data.state != PlayerData.State.IN_PROGRESS) {
            return false;
        }
        data.clearMission();
        plugin.data().markDirty();
        plugin.bars().refresh(player);
        return true;
    }

    // ---------------------------------------------------------------- progreso
    public void addProgress(Player player, MissionType type, Collection<String> keys, int amount) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        if (data.state != PlayerData.State.IN_PROGRESS) {
            return;
        }
        Mission mission = plugin.registry().get(data.missionId);
        if (mission == null || mission.type() != type || !mission.matches(keys)) {
            return;
        }

        data.progress = Math.min(mission.amount(), data.progress + amount);
        plugin.data().markDirty();

        if (data.progress >= mission.amount()) {
            complete(player, data, mission);
        } else {
            plugin.bars().refresh(player);
        }
    }

    private void complete(Player player, PlayerData data, Mission mission) {
        data.state = PlayerData.State.COMPLETED;
        data.progress = mission.amount();
        data.completed.add(mission.id());
        data.offer = generateOffer();
        plugin.data().markDirty();
        plugin.data().save();

        plugin.bars().refresh(player);
        Msg.send(player, "&a¡Misión completada: &f" + mission.name() + "&a! Habla con el aldeano para ver tu oferta.");
        player.showTitle(Title.title(Msg.c("&a¡Misión completada!"), Msg.c("&7Habla con el aldeano")));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    // ---------------------------------------------------------------- ofertas
    private Offer generateOffer() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int min = Math.max(1, plugin.getConfig().getInt("tradeo.lingotes-min", 2));
        int max = Math.max(min, plugin.getConfig().getInt("tradeo.lingotes-max", 8));
        int ingots = min + random.nextInt(max - min + 1);

        int enchantCount = Math.max(1, plugin.getConfig().getInt("tradeo.encantamiento.cantidad", 2));
        int levelMin = Math.max(1, plugin.getConfig().getInt("tradeo.encantamiento.nivel-min", 4));
        int levelMax = Math.max(levelMin, plugin.getConfig().getInt("tradeo.encantamiento.nivel-max", 6));

        ItemSet set = ITEM_SETS.get(random.nextInt(ITEM_SETS.size()));

        // Solo se usan encantamientos que existan en esta versión del juego
        List<String> pool = new ArrayList<>();
        for (String key : set.enchants()) {
            if (Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key)) != null) {
                pool.add(key);
            }
        }
        Collections.shuffle(pool, random);

        Map<String, Integer> enchants = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(enchantCount, pool.size()); i++) {
            enchants.put(pool.get(i), levelMin + random.nextInt(levelMax - levelMin + 1));
        }

        return new Offer(set.diamond(), set.netherite(), ingots, enchants);
    }
}

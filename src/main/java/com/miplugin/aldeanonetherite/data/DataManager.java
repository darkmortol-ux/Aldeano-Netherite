package com.miplugin.aldeanonetherite.data;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Guarda en data.yml el progreso de los jugadores y la ubicación del aldeano. */
public final class DataManager {

    private final AldeanoNetheritePlugin plugin;
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private File file;
    private YamlConfiguration yaml = new YamlConfiguration();
    private boolean dirty;

    public DataManager(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------- carga / guardado
    public void load() {
        plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "data.yml");
        yaml = YamlConfiguration.loadConfiguration(file);
        players.clear();

        ConfigurationSection section = yaml.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            ConfigurationSection s = section.getConfigurationSection(key);
            if (s == null) {
                continue;
            }
            PlayerData pd = new PlayerData();
            try {
                pd.state = PlayerData.State.valueOf(s.getString("state", "NONE"));
            } catch (IllegalArgumentException ex) {
                pd.state = PlayerData.State.NONE;
            }
            pd.missionId = s.getInt("mission", -1);
            pd.progress = s.getInt("progress", 0);
            pd.completed.addAll(s.getIntegerList("completed"));

            ConfigurationSection o = s.getConfigurationSection("offer");
            if (o != null) {
                Material diamond = Material.matchMaterial(o.getString("diamond", ""));
                Material netherite = Material.matchMaterial(o.getString("netherite", ""));
                if (diamond != null && netherite != null) {
                    Map<String, Integer> enchants = new LinkedHashMap<>();
                    ConfigurationSection es = o.getConfigurationSection("enchants");
                    if (es != null) {
                        for (String k : es.getKeys(false)) {
                            enchants.put(k, es.getInt(k));
                        }
                    }
                    pd.offer = new Offer(diamond, netherite, o.getInt("ingots", 2), enchants);
                }
            }
            players.put(uuid, pd);
        }
    }

    public void save() {
        yaml.set("players", null);
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            PlayerData pd = entry.getValue();
            if (pd.state == PlayerData.State.NONE && pd.completed.isEmpty()) {
                continue;
            }
            String base = "players." + entry.getKey() + ".";
            yaml.set(base + "state", pd.state.name());
            yaml.set(base + "mission", pd.missionId);
            yaml.set(base + "progress", pd.progress);
            yaml.set(base + "completed", new ArrayList<>(pd.completed));
            if (pd.offer != null) {
                yaml.set(base + "offer.diamond", pd.offer.diamond().name());
                yaml.set(base + "offer.netherite", pd.offer.netherite().name());
                yaml.set(base + "offer.ingots", pd.offer.ingots());
                for (Map.Entry<String, Integer> e : pd.offer.enchants().entrySet()) {
                    yaml.set(base + "offer.enchants." + e.getKey(), e.getValue());
                }
            }
        }
        try {
            yaml.save(file);
            dirty = false;
        } catch (IOException ex) {
            plugin.getLogger().severe("No se pudo guardar data.yml: " + ex.getMessage());
        }
    }

    public void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    public void markDirty() {
        dirty = true;
    }

    // ---------------------------------------------------------------- jugadores
    public PlayerData get(UUID uuid) {
        return players.computeIfAbsent(uuid, k -> new PlayerData());
    }

    // ---------------------------------------------------------------- aldeano
    public Location getVillagerLocation() {
        String worldName = yaml.getString("villager.world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world,
                yaml.getDouble("villager.x"),
                yaml.getDouble("villager.y"),
                yaml.getDouble("villager.z"),
                (float) yaml.getDouble("villager.yaw"),
                0f);
    }

    public UUID getVillagerUuid() {
        String raw = yaml.getString("villager.uuid");
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public void setVillager(Location loc, UUID uuid) {
        yaml.set("villager.world", loc.getWorld().getName());
        yaml.set("villager.x", loc.getX());
        yaml.set("villager.y", loc.getY());
        yaml.set("villager.z", loc.getZ());
        yaml.set("villager.yaw", (double) loc.getYaw());
        yaml.set("villager.uuid", uuid.toString());
        save();
    }

    public void clearVillager() {
        yaml.set("villager", null);
        save();
    }
}

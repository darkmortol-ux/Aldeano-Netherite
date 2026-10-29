package com.miplugin.aldeanonetherite.villager;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import com.miplugin.aldeanonetherite.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Crea, busca y elimina el aldeano comerciante especial. */
public final class VillagerManager {

    private final AldeanoNetheritePlugin plugin;
    private final NamespacedKey tagKey;

    public VillagerManager(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
        this.tagKey = new NamespacedKey(plugin, "aldeano_netherite");
    }

    /** True si la entidad es el aldeano de este plugin. */
    public boolean isMerchant(Entity entity) {
        return entity instanceof Villager
                && entity.getPersistentDataContainer().has(tagKey, PersistentDataType.BYTE);
    }

    /** Coloca el aldeano en la posición del jugador (y quita el anterior). */
    public void place(Player player) {
        remove();
        Location l = player.getLocation();
        Location spot = new Location(
                l.getWorld(),
                l.getBlockX() + 0.5,
                l.getY(),
                l.getBlockZ() + 0.5,
                Location.normalizeYaw(l.getYaw() + 180f), // mira hacia el jugador
                0f);
        Villager villager = create(spot);
        plugin.data().setVillager(spot, villager.getUniqueId());
    }

    /** Quita el aldeano y olvida su ubicación. */
    public void remove() {
        Location saved = plugin.data().getVillagerLocation();
        List<Entity> toRemove = new ArrayList<>();

        UUID id = plugin.data().getVillagerUuid();
        if (id != null) {
            Entity byId = Bukkit.getEntity(id);
            if (byId != null && isMerchant(byId)) {
                toRemove.add(byId);
            }
        }
        if (saved != null && saved.getWorld() != null) {
            saved.getChunk().load();
            for (Entity nearby : saved.getWorld().getNearbyEntities(saved, 4, 4, 4)) {
                if (isMerchant(nearby) && !toRemove.contains(nearby)) {
                    toRemove.add(nearby);
                }
            }
        }
        for (Entity entity : toRemove) {
            entity.remove();
        }
        plugin.data().clearVillager();
    }

    /** Si hay una ubicación guardada y el aldeano no existe, lo vuelve a crear. */
    public void ensure() {
        Location saved = plugin.data().getVillagerLocation();
        if (saved == null || saved.getWorld() == null) {
            return;
        }
        saved.getChunk().load();

        UUID id = plugin.data().getVillagerUuid();
        if (id != null) {
            Entity byId = Bukkit.getEntity(id);
            if (byId != null && isMerchant(byId)) {
                return;
            }
        }
        for (Entity nearby : saved.getWorld().getNearbyEntities(saved, 4, 4, 4)) {
            if (isMerchant(nearby)) {
                return;
            }
        }
        Villager villager = create(saved);
        plugin.data().setVillager(saved, villager.getUniqueId());
        plugin.getLogger().info("Aldeano recreado en su ubicación guardada.");
    }

    private Villager create(Location location) {
        String name = plugin.getConfig().getString("aldeano.nombre", "&6&lComerciante de Netherite");
        return location.getWorld().spawn(location, Villager.class, villager -> {
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setPersistent(true);
            villager.setRemoveWhenFarAway(false);
            villager.setSilent(true);
            villager.setCollidable(false);
            villager.setProfession(Villager.Profession.ARMORER);
            villager.setVillagerLevel(5);
            villager.customName(Msg.c(name));
            villager.setCustomNameVisible(true);
            villager.getPersistentDataContainer().set(tagKey, PersistentDataType.BYTE, (byte) 1);
        });
    }
}

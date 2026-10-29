package com.miplugin.aldeanonetherite.listener;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/** Interacción con el aldeano y protección contra daño. */
public final class VillagerListener implements Listener {

    private final AldeanoNetheritePlugin plugin;

    public VillagerListener(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!plugin.villagers().isMerchant(event.getRightClicked())) {
            return;
        }
        // Se cancela siempre para que no se abra el menú normal de aldeanos
        event.setCancelled(true);
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // el evento llega dos veces (mano principal y secundaria)
        }
        plugin.missions().interact(event.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (plugin.villagers().isMerchant(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}

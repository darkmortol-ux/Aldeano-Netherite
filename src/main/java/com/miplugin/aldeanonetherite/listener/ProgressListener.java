package com.miplugin.aldeanonetherite.listener;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import com.miplugin.aldeanonetherite.mission.MissionType;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Cuenta el progreso de las misiones (matar, minar, fabricar, pescar y criar). */
public final class ProgressListener implements Listener {

    private static final int MAX_PLACED_TRACKED = 50_000;

    private final AldeanoNetheritePlugin plugin;
    /** Bloques puestos por jugadores: no cuentan para misiones de minar. */
    private final Set<String> placedBlocks = new HashSet<>();

    public ProgressListener(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || plugin.villagers().isMerchant(entity)) {
            return;
        }
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        List<String> keys = new ArrayList<>();
        keys.add(entity.getType().name());
        if (entity instanceof Enemy) {
            keys.add("HOSTILE");
        }
        plugin.missions().addProgress(killer, MissionType.KILL, keys, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (placedBlocks.size() > MAX_PLACED_TRACKED) {
            placedBlocks.clear();
        }
        placedBlocks.add(key(event.getBlockPlaced()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Block block = event.getBlock();
        if (placedBlocks.remove(key(block))) {
            return; // el bloque lo puso un jugador
        }
        plugin.missions().addProgress(player, MissionType.MINE, List.of(block.getType().name()), 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        plugin.missions().addProgress(event.getPlayer(), MissionType.FISH, List.of("ANY"), 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack result = event.getRecipe().getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }
        int crafts = 1;
        if (event.isShiftClick()) {
            int min = Integer.MAX_VALUE;
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item != null && !item.getType().isAir()) {
                    min = Math.min(min, item.getAmount());
                }
            }
            if (min != Integer.MAX_VALUE) {
                crafts = Math.max(1, min);
            }
        }
        int total = crafts * Math.max(1, result.getAmount());
        plugin.missions().addProgress(player, MissionType.CRAFT, List.of(result.getType().name()), total);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) {
            return;
        }
        plugin.missions().addProgress(player, MissionType.BREED, List.of(event.getEntityType().name()), 1);
    }

    private String key(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }
}

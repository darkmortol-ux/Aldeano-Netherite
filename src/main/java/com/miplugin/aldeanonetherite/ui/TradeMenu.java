package com.miplugin.aldeanonetherite.ui;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import com.miplugin.aldeanonetherite.data.Offer;
import com.miplugin.aldeanonetherite.data.PlayerData;
import com.miplugin.aldeanonetherite.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menú con los 2 intercambios de la misión completada.
 * Slot 11: intercambio base. Slot 15: intercambio mejorado (con plantilla).
 */
public final class TradeMenu implements InventoryHolder {

    private static final int SLOT_BASE = 11;
    private static final int SLOT_ENHANCED = 15;
    private static final int SIZE = 27;

    private final AldeanoNetheritePlugin plugin;
    private final Inventory inventory;

    private TradeMenu(AldeanoNetheritePlugin plugin, Player player, Offer offer) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, SIZE, Msg.c("&8Comerciante de Netherite"));
        build(player, offer);
    }

    public static void open(AldeanoNetheritePlugin plugin, Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        if (data.state != PlayerData.State.COMPLETED || data.offer == null) {
            return;
        }
        TradeMenu menu = new TradeMenu(plugin, player, data.offer);
        player.openInventory(menu.getInventory());
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    // ---------------------------------------------------------------- construcción
    private void build(Player player, Offer offer) {
        // Relleno
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }

        // Cabecera
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Msg.item("&6Recompensa de misión"));
        infoMeta.lore(List.of(
                Msg.item("&7Elige UN intercambio."),
                Msg.item("&7El de la derecha es mejor, pero")
                        .append(Msg.item(" &7necesita una plantilla."))));
        info.setItemMeta(infoMeta);
        inventory.setItem(4, info);

        int diamondCount = countItem(player, offer.diamond());
        int ingotCount = countItem(player, Material.NETHERITE_INGOT);
        int templateCount = countItem(player, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);

        // Intercambio base
        boolean baseOk = diamondCount >= 1 && ingotCount >= offer.ingots();
        ItemStack base = new ItemStack(offer.netherite());
        ItemMeta baseMeta = base.getItemMeta();
        List<Component> baseLore = new ArrayList<>();
        baseLore.add(Msg.item("&6&lIntercambio base"));
        baseLore.add(Msg.item("&7Requiere:"));
        baseLore.add(requirement(diamondCount >= 1, 1, offer.diamond()));
        baseLore.add(requirement(ingotCount >= offer.ingots(), offer.ingots(), Material.NETHERITE_INGOT));
        baseLore.add(Msg.item(" "));
        baseLore.add(Msg.item("&8Conserva los encantamientos de tu pieza."));
        baseLore.add(Msg.item(baseOk ? "&eClic para intercambiar" : "&cTe faltan materiales"));
        baseMeta.lore(baseLore);
        base.setItemMeta(baseMeta);
        inventory.setItem(SLOT_BASE, base);

        // Intercambio mejorado
        boolean enhancedOk = baseOk && templateCount >= 1;
        ItemStack enhanced = new ItemStack(offer.netherite());
        ItemMeta enhancedMeta = enhanced.getItemMeta();
        for (Map.Entry<String, Integer> e : offer.enchants().entrySet()) {
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(e.getKey()));
            if (enchantment != null) {
                enhancedMeta.addEnchant(enchantment, e.getValue(), true);
            }
        }
        List<Component> enhancedLore = new ArrayList<>();
        enhancedLore.add(Msg.item("&d&lIntercambio mejorado"));
        enhancedLore.add(Msg.item("&7Requiere:"));
        enhancedLore.add(requirement(diamondCount >= 1, 1, offer.diamond()));
        enhancedLore.add(requirement(ingotCount >= offer.ingots(), offer.ingots(), Material.NETHERITE_INGOT));
        enhancedLore.add(requirement(templateCount >= 1, 1, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
        enhancedLore.add(Msg.item(" "));
        enhancedLore.add(Msg.item("&dIncluye encantamientos de nivel alto."));
        enhancedLore.add(Msg.item(enhancedOk ? "&eClic para intercambiar" : "&cTe faltan materiales"));
        enhancedMeta.lore(enhancedLore);
        enhanced.setItemMeta(enhancedMeta);
        inventory.setItem(SLOT_ENHANCED, enhanced);
    }

    private Component requirement(boolean has, int quantity, Material material) {
        return Msg.item(has ? "&a✔ " : "&c✘ ")
                .append(Component.text(quantity + "x ", NamedTextColor.WHITE))
                .append(Component.translatable(material.translationKey(), NamedTextColor.WHITE));
    }

    // ---------------------------------------------------------------- clics
    public void handleClick(Player player, int slot) {
        if (slot == SLOT_BASE) {
            execute(player, false);
        } else if (slot == SLOT_ENHANCED) {
            execute(player, true);
        }
    }

    private void execute(Player player, boolean enhanced) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        Offer offer = data.offer;
        if (data.state != PlayerData.State.COMPLETED || offer == null) {
            Bukkit.getScheduler().runTask(plugin, player::closeInventory);
            return;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getStorageContents();

        int diamondIndex = -1;
        int ingots = 0;
        int templates = 0;
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null) {
                continue;
            }
            Material type = stack.getType();
            if (type == offer.diamond() && diamondIndex < 0) {
                diamondIndex = i;
            } else if (type == Material.NETHERITE_INGOT) {
                ingots += stack.getAmount();
            } else if (type == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
                templates += stack.getAmount();
            }
        }

        boolean ok = diamondIndex >= 0 && ingots >= offer.ingots() && (!enhanced || templates >= 1);
        if (!ok) {
            Msg.send(player, "&cNo tienes todos los materiales que pide este intercambio.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Se conserva la información de la pieza de diamante (encantamientos, desgaste, etc.)
        ItemStack source = contents[diamondIndex];
        ItemMeta sourceMeta = source.getItemMeta();
        if (source.getAmount() > 1) {
            source.setAmount(source.getAmount() - 1);
        } else {
            contents[diamondIndex] = null;
        }
        take(contents, Material.NETHERITE_INGOT, offer.ingots());
        if (enhanced) {
            take(contents, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
        }
        inv.setStorageContents(contents);

        ItemStack result = new ItemStack(offer.netherite());
        if (sourceMeta != null) {
            result.setItemMeta(sourceMeta);
        }
        if (enhanced) {
            ItemMeta meta = result.getItemMeta();
            for (Map.Entry<String, Integer> e : offer.enchants().entrySet()) {
                Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(e.getKey()));
                if (enchantment != null) {
                    meta.addEnchant(enchantment, e.getValue(), true);
                }
            }
            result.setItemMeta(meta);
        }

        Map<Integer, ItemStack> leftovers = inv.addItem(result);
        for (ItemStack left : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        }

        // La misión queda cerrada: la próxima vez el aldeano da una nueva
        data.clearMission();
        plugin.data().markDirty();
        plugin.data().save();
        plugin.bars().refresh(player);

        Msg.send(player, enhanced
                ? "&d¡Intercambio mejorado realizado! Disfruta tu equipo."
                : "&a¡Intercambio realizado! Vuelve conmigo para otra misión.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        Bukkit.getScheduler().runTask(plugin, player::closeInventory);
    }

    // ---------------------------------------------------------------- utilidades de inventario
    private int countItem(Player player, Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private void take(ItemStack[] contents, Material material, int amount) {
        int remaining = amount;
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != material) {
                continue;
            }
            int taken = Math.min(remaining, stack.getAmount());
            if (taken == stack.getAmount()) {
                contents[i] = null;
            } else {
                stack.setAmount(stack.getAmount() - taken);
            }
            remaining -= taken;
        }
    }
}

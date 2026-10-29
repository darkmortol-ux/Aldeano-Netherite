package com.miplugin.aldeanonetherite.data;

import org.bukkit.Material;

import java.util.Map;

/**
 * Oferta que el aldeano muestra al completar una misión.
 *
 * @param diamond   pieza de diamante que se pide
 * @param netherite pieza de netherite que se entrega
 * @param ingots    lingotes de netherite que se piden (2 a 8)
 * @param enchants  encantamientos del trato mejorado (clave -> nivel)
 */
public record Offer(Material diamond, Material netherite, int ingots, Map<String, Integer> enchants) {
}

package com.miplugin.aldeanonetherite.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

/** Utilidades para mensajes con colores (&). */
public final class Msg {

    public static final String PREFIX = "&6[Aldeano] &r";
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Msg() {
    }

    /** Texto con colores. */
    public static Component c(String text) {
        return LEGACY.deserialize(text);
    }

    /** Texto para items (sin cursiva). */
    public static Component item(String text) {
        return c(text).decoration(TextDecoration.ITALIC, false);
    }

    /** Envía un mensaje con el prefijo del plugin. */
    public static void send(CommandSender to, String text) {
        to.sendMessage(c(PREFIX + text));
    }
}

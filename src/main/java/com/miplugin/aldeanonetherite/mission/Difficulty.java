package com.miplugin.aldeanonetherite.mission;

import net.kyori.adventure.bossbar.BossBar;

public enum Difficulty {
    MEDIA_ALTA("Media-Alta", "&e", BossBar.Color.YELLOW),
    ALTA("Alta", "&c", BossBar.Color.RED),
    MUY_ALTA("Muy alta", "&d", BossBar.Color.PURPLE);

    private final String label;
    private final String chatColor;
    private final BossBar.Color barColor;

    Difficulty(String label, String chatColor, BossBar.Color barColor) {
        this.label = label;
        this.chatColor = chatColor;
        this.barColor = barColor;
    }

    public String label() {
        return label;
    }

    public String chatColor() {
        return chatColor;
    }

    public BossBar.Color barColor() {
        return barColor;
    }
}

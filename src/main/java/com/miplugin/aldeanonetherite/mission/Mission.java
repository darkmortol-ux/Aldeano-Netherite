package com.miplugin.aldeanonetherite.mission;

import java.util.Collection;
import java.util.Set;

public final class Mission {

    private final int id;
    private final String name;
    private final String description;
    private final Difficulty difficulty;
    private final MissionType type;
    private final Set<String> targets;
    private final int amount;

    public Mission(int id, String name, String description, Difficulty difficulty,
                   MissionType type, Set<String> targets, int amount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.type = type;
        this.targets = targets;
        this.amount = amount;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public MissionType type() {
        return type;
    }

    public int amount() {
        return amount;
    }

    /** True si alguna de las claves coincide con un objetivo de la misión. */
    public boolean matches(Collection<String> keys) {
        for (String key : keys) {
            if (targets.contains(key)) {
                return true;
            }
        }
        return false;
    }
}

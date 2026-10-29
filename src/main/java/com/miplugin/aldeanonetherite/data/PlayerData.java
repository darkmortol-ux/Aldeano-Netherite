package com.miplugin.aldeanonetherite.data;

import java.util.HashSet;
import java.util.Set;

/** Estado de misión de un jugador. */
public final class PlayerData {

    public enum State { NONE, IN_PROGRESS, COMPLETED }

    public State state = State.NONE;
    public int missionId = -1;
    public int progress = 0;
    public final Set<Integer> completed = new HashSet<>();
    public Offer offer;

    /** Deja al jugador sin misión activa (mantiene el historial). */
    public void clearMission() {
        state = State.NONE;
        missionId = -1;
        progress = 0;
        offer = null;
    }

    /** Borra todo, incluido el historial de misiones hechas. */
    public void resetAll() {
        clearMission();
        completed.clear();
    }
}

package com.gamearcade.game_arcade_backend.ludo;

import java.util.Map;

public class LudoStartRequest {
    private Map<String, String> playerNames;

    public Map<String, String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(Map<String, String> playerNames) {
        this.playerNames = playerNames;
    }
}
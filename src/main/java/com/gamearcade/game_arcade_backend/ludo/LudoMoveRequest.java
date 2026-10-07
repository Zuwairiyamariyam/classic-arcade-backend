package com.gamearcade.game_arcade_backend.ludo;

public class LudoMoveRequest {
    private String color;
    private int tokenId;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }
}
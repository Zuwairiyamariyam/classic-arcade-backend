package com.gamearcade.game_arcade_backend.model;

public class RPSResponse {

    private String playerChoice;
    private String computerChoice;
    private String result;

    public RPSResponse(String playerChoice, String computerChoice, String result) {
        this.playerChoice = playerChoice;
        this.computerChoice = computerChoice;
        this.result = result;
    }

    public String getPlayerChoice() {
        return playerChoice;
    }

    public String getComputerChoice() {
        return computerChoice;
    }

    public String getResult() {
        return result;
    }
}

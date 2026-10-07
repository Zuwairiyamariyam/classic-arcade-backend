package com.gamearcade.game_arcade_backend.model;

public class TicTacToeResponse {

    private String[][] board;
    private String result;
    private boolean gameOver;

    public TicTacToeResponse(String[][] board, String result, boolean gameOver) {
        this.board = board;
        this.result = result;
        this.gameOver = gameOver;
    }

    public String[][] getBoard() {
        return board;
    }

    public String getResult() {
        return result;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
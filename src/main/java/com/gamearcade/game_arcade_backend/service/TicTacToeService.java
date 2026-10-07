package com.gamearcade.game_arcade_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.gamearcade.game_arcade_backend.model.TicTacToeRequest;
import com.gamearcade.game_arcade_backend.model.TicTacToeResponse;

@Service
public class TicTacToeService {

    private String[][] board = new String[3][3];

    private final Random random = new Random();

    public TicTacToeService() {
        resetBoard();
    }

    // -----------------------------
    // RESET BOARD
    // -----------------------------

    public void resetBoard() {

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                board[row][col] = "";
            }
        }
    }

    // -----------------------------
    // PLAYER MOVE
    // -----------------------------

    public TicTacToeResponse playGame(TicTacToeRequest request) {

        String playerSymbol = request.getPlayerSymbol().toUpperCase();

        String computerSymbol =
                playerSymbol.equals("X") ? "O" : "X";

        int row = request.getRow();
        int col = request.getCol();

        String difficulty = request.getDifficulty();

        if (difficulty == null || difficulty.isEmpty()) {
            difficulty = "MEDIUM";
        }

        difficulty = difficulty.toUpperCase();

        // Check position
        if (row < 0 || row > 2 || col < 0 || col > 2) {

            return new TicTacToeResponse(
                    board,
                    "INVALID MOVE",
                    false
            );
        }

        // Check occupied cell
        if (!board[row][col].equals("")) {

            return new TicTacToeResponse(
                    board,
                    "CELL ALREADY OCCUPIED",
                    false
            );
        }

        // Player makes move
        board[row][col] = playerSymbol;

        // Check player win
        if (checkWinner(playerSymbol)) {

            return new TicTacToeResponse(
                    board,
                    "YOU WIN",
                    true
            );
        }

        // Check draw
        if (isBoardFull()) {

            return new TicTacToeResponse(
                    board,
                    "DRAW",
                    true
            );
        }

        // -----------------------------
        // COMPUTER MOVE
        // -----------------------------

        makeComputerMove(computerSymbol, playerSymbol, difficulty);

        // Check computer win
        if (checkWinner(computerSymbol)) {

            return new TicTacToeResponse(
                    board,
                    "COMPUTER WINS",
                    true
            );
        }

        // Check draw after computer move
        if (isBoardFull()) {

            return new TicTacToeResponse(
                    board,
                    "DRAW",
                    true
            );
        }

        return new TicTacToeResponse(
                board,
                "YOUR TURN",
                false
        );
    }

    // -----------------------------
    // COMPUTER MOVE
    // -----------------------------

    private void makeComputerMove(
            String computerSymbol,
            String playerSymbol,
            String difficulty) {

        if (difficulty.equals("EASY")) {

            makeRandomMove(computerSymbol);

        }
        else if (difficulty.equals("MEDIUM")) {

            makeMediumMove(computerSymbol, playerSymbol);

        }
        else {

            makeHardMove(computerSymbol, playerSymbol);
        }
    }

    // -----------------------------
    // EASY MODE
    // -----------------------------

    private void makeRandomMove(String computerSymbol) {

        List<int[]> emptyCells = getEmptyCells();

        if (emptyCells.isEmpty()) {
            return;
        }

        int[] move =
                emptyCells.get(
                        random.nextInt(emptyCells.size())
                );

        board[move[0]][move[1]] = computerSymbol;
    }

    // -----------------------------
    // MEDIUM MODE
    // -----------------------------

    private void makeMediumMove(
            String computerSymbol,
            String playerSymbol) {

        // First try to win
        int[] winningMove =
                findWinningMove(computerSymbol);

        if (winningMove != null) {

            board[winningMove[0]][winningMove[1]]
                    = computerSymbol;

            return;
        }

        // Then block player
        int[] blockingMove =
                findWinningMove(playerSymbol);

        if (blockingMove != null) {

            board[blockingMove[0]][blockingMove[1]]
                    = computerSymbol;

            return;
        }

        // Otherwise use limited Minimax
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int[] move : getEmptyCells()) {

            board[move[0]][move[1]] = computerSymbol;

            int score =
                    minimax(
                            board,
                            0,
                            false,
                            computerSymbol,
                            playerSymbol,
                            2
                    );

            board[move[0]][move[1]] = "";

            if (score > bestScore) {

                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove != null) {

            board[bestMove[0]][bestMove[1]]
                    = computerSymbol;
        }
    }

    // -----------------------------
    // HARD MODE
    // -----------------------------

    private void makeHardMove(
            String computerSymbol,
            String playerSymbol) {

        int bestScore = Integer.MIN_VALUE;

        int[] bestMove = null;

        for (int[] move : getEmptyCells()) {

            board[move[0]][move[1]] = computerSymbol;

            int score =
                    minimax(
                            board,
                            0,
                            false,
                            computerSymbol,
                            playerSymbol,
                            10
                    );

            board[move[0]][move[1]] = "";

            if (score > bestScore) {

                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove != null) {

            board[bestMove[0]][bestMove[1]]
                    = computerSymbol;
        }
    }

    // -----------------------------
    // MINIMAX
    // -----------------------------

    private int minimax(
            String[][] currentBoard,
            int depth,
            boolean maximizing,
            String computerSymbol,
            String playerSymbol,
            int maxDepth) {

        if (checkWinnerOnBoard(
                currentBoard,
                computerSymbol)) {

            return 10 - depth;
        }

        if (checkWinnerOnBoard(
                currentBoard,
                playerSymbol)) {

            return depth - 10;
        }

        if (isBoardFull(currentBoard)) {

            return 0;
        }

        if (depth >= maxDepth) {

            return evaluatePosition(
                    currentBoard,
                    computerSymbol,
                    playerSymbol
            );
        }

        if (maximizing) {

            int bestScore = Integer.MIN_VALUE;

            for (int row = 0; row < 3; row++) {

                for (int col = 0; col < 3; col++) {

                    if (currentBoard[row][col].equals("")) {

                        currentBoard[row][col]
                                = computerSymbol;

                        int score =
                                minimax(
                                        currentBoard,
                                        depth + 1,
                                        false,
                                        computerSymbol,
                                        playerSymbol,
                                        maxDepth
                                );

                        currentBoard[row][col] = "";

                        bestScore =
                                Math.max(
                                        bestScore,
                                        score
                                );
                    }
                }
            }

            return bestScore;

        } else {

            int bestScore = Integer.MAX_VALUE;

            for (int row = 0; row < 3; row++) {

                for (int col = 0; col < 3; col++) {

                    if (currentBoard[row][col].equals("")) {

                        currentBoard[row][col]
                                = playerSymbol;

                        int score =
                                minimax(
                                        currentBoard,
                                        depth + 1,
                                        true,
                                        computerSymbol,
                                        playerSymbol,
                                        maxDepth
                                );

                        currentBoard[row][col] = "";

                        bestScore =
                                Math.min(
                                        bestScore,
                                        score
                                );
                    }
                }
            }

            return bestScore;
        }
    }

    // -----------------------------
    // POSITION EVALUATION
    // -----------------------------

    private int evaluatePosition(
            String[][] currentBoard,
            String computerSymbol,
            String playerSymbol) {

        int score = 0;

        // Center
        if (currentBoard[1][1]
                .equals(computerSymbol)) {

            score += 2;

        } else if (currentBoard[1][1]
                .equals(playerSymbol)) {

            score -= 2;
        }

        // Corners
        int[][] corners = {
                {0, 0},
                {0, 2},
                {2, 0},
                {2, 2}
        };

        for (int[] corner : corners) {

            String cell =
                    currentBoard[
                            corner[0]
                    ][
                            corner[1]
                    ];

            if (cell.equals(computerSymbol)) {

                score++;

            } else if (cell.equals(playerSymbol)) {

                score--;
            }
        }

        return score;
    }

    // -----------------------------
    // FIND WINNING MOVE
    // -----------------------------

    private int[] findWinningMove(String symbol) {

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                if (board[row][col].equals("")) {

                    board[row][col] = symbol;

                    boolean wins =
                            checkWinner(symbol);

                    board[row][col] = "";

                    if (wins) {

                        return new int[]{
                                row,
                                col
                        };
                    }
                }
            }
        }

        return null;
    }

    // -----------------------------
    // GET EMPTY CELLS
    // -----------------------------

    private List<int[]> getEmptyCells() {

        List<int[]> emptyCells =
                new ArrayList<>();

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                if (board[row][col].equals("")) {

                    emptyCells.add(
                            new int[]{
                                    row,
                                    col
                            }
                    );
                }
            }
        }

        return emptyCells;
    }

    // -----------------------------
    // CHECK WINNER - CURRENT BOARD
    // -----------------------------

    private boolean checkWinner(String symbol) {

        return checkWinnerOnBoard(
                board,
                symbol
        );
    }

    // -----------------------------
    // CHECK WINNER - ANY BOARD
    // -----------------------------

    private boolean checkWinnerOnBoard(
            String[][] currentBoard,
            String symbol) {

        // Rows
        for (int row = 0; row < 3; row++) {

            if (
                    currentBoard[row][0].equals(symbol) &&
                    currentBoard[row][1].equals(symbol) &&
                    currentBoard[row][2].equals(symbol)
            ) {

                return true;
            }
        }

        // Columns
        for (int col = 0; col < 3; col++) {

            if (
                    currentBoard[0][col].equals(symbol) &&
                    currentBoard[1][col].equals(symbol) &&
                    currentBoard[2][col].equals(symbol)
            ) {

                return true;
            }
        }

        // Main diagonal
        if (
                currentBoard[0][0].equals(symbol) &&
                currentBoard[1][1].equals(symbol) &&
                currentBoard[2][2].equals(symbol)
        ) {

            return true;
        }

        // Other diagonal
        if (
                currentBoard[0][2].equals(symbol) &&
                currentBoard[1][1].equals(symbol) &&
                currentBoard[2][0].equals(symbol)
        ) {

            return true;
        }

        return false;
    }

    // -----------------------------
    // CHECK FULL BOARD - CURRENT
    // -----------------------------

    private boolean isBoardFull() {

        return isBoardFull(board);
    }

    // -----------------------------
    // CHECK FULL BOARD - ANY BOARD
    // -----------------------------

    private boolean isBoardFull(
            String[][] currentBoard) {

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                if (currentBoard[row][col].equals("")) {

                    return false;
                }
            }
        }

        return true;
    }
}
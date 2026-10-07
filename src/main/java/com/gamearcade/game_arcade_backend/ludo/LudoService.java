package com.gamearcade.game_arcade_backend.ludo;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LudoService {

    // Clockwise player order: Red -> Green -> Yellow -> Blue
    public static final List<String> PLAYERS = List.of("red", "green", "yellow", "blue");

    // Starting positions on the 0-51 common track
    private static final Map<String, Integer> START_CELLS = Map.of(
            "red", 0,
            "green", 13,
            "yellow", 26,
            "blue", 39
    );

    // Standard 8 Safe star squares
    private static final Set<Integer> SAFE_CELLS = Set.of(0, 8, 13, 21, 26, 34, 39, 47);

    private final Random random = new Random();

    // 4 tokens per player: -1 (base), 0-50 (outer track), 51-55 (home lane), 56 (finished)
    private Map<String, List<Integer>> tokens;
    private String activePlayer;
    private Integer diceNumber;
    private boolean hasRolled;
    private boolean gameOver;
    private String message;
    private Map<String, Integer> placements;
    private Map<String, String> playerNames = createDefaultNames();
    private int consecutiveSixCount = 0;

    public LudoService() {
        resetGameInternal();
        message = "Red's turn! Roll the dice!";
    }

    private Map<String, String> createDefaultNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("red", "Player 1");
        names.put("green", "Player 2");
        names.put("yellow", "Player 3");
        names.put("blue", "Player 4");
        return names;
    }

    public synchronized Map<String, Object> startGame(Map<String, String> names) {
        playerNames = new LinkedHashMap<>();
        if (names == null) {
            names = createDefaultNames();
        }

        for (String color : PLAYERS) {
            String name = names.get(color);
            if (name == null || name.trim().isEmpty()) {
                name = color.toUpperCase();
            }
            playerNames.put(color, name.trim());
        }

        resetGameInternal();
        activePlayer = "red";
        message = getPlayerName("red") + "'s turn! Roll the dice!";
        return getState();
    }

    public synchronized Map<String, Object> resetGame() {
        resetGameInternal();
        activePlayer = "red";
        message = getPlayerName("red") + "'s turn! Roll the dice!";
        return getState();
    }

    private void resetGameInternal() {
        tokens = new LinkedHashMap<>();
        for (String player : PLAYERS) {
            tokens.put(player, new ArrayList<>(Arrays.asList(-1, -1, -1, -1)));
        }
        placements = new LinkedHashMap<>();
        diceNumber = null;
        hasRolled = false;
        gameOver = false;
        consecutiveSixCount = 0;
    }

    public synchronized Map<String, Object> getState() {
        Map<String, Object> state = new LinkedHashMap<>();
        Map<String, List<Integer>> tokenCopy = new LinkedHashMap<>();
        for (String p : PLAYERS) {
            tokenCopy.put(p, new ArrayList<>(tokens.get(p)));
        }

        state.put("tokens", tokenCopy);
        state.put("activePlayer", activePlayer);
        state.put("diceNumber", diceNumber);
        state.put("hasRolled", hasRolled);
        state.put("gameOver", gameOver);
        state.put("placements", new LinkedHashMap<>(placements));
        state.put("message", message);
        state.put("playerNames", new LinkedHashMap<>(playerNames));
        state.put("consecutiveSixes", consecutiveSixCount);
        return state;
    }

    public synchronized Map<String, Object> rollDice() {
        if (gameOver) {
            throw new IllegalStateException("Game is already over. Please restart.");
        }
        if (activePlayer == null) {
            throw new IllegalStateException("No active player.");
        }
        if (hasRolled) {
            throw new IllegalStateException("You already rolled. Move a token first.");
        }

        int rolledValue = random.nextInt(6) + 1;
        diceNumber = rolledValue;
        hasRolled = true;

        String currentPlayer = activePlayer;
        String currentName = getPlayerName(currentPlayer);

        if (rolledValue == 6) {
            consecutiveSixCount++;
        } else {
            consecutiveSixCount = 0;
        }

        // Three 6s Rule: Cancel turn immediately!
        if (consecutiveSixCount >= 3) {
            consecutiveSixCount = 0;
            hasRolled = false;
            activePlayer = getNextPlayer(currentPlayer);
            message = currentName + " rolled 3 consecutive SIXES! Turn cancelled. " + getPlayerName(activePlayer) + "'s turn!";
            return getState();
        }

        List<Integer> validTokens = getValidTokenIds(currentPlayer, rolledValue);

        // No legal move -> pass turn
        if (validTokens.isEmpty()) {
            hasRolled = false;
            consecutiveSixCount = 0;
            activePlayer = getNextPlayer(currentPlayer);
            message = currentName + " rolled " + rolledValue + " with no valid moves. " + getPlayerName(activePlayer) + "'s turn!";
            return getState();
        }

        message = currentName + " rolled " + rolledValue + "! Click on your coin to move.";
        return getState();
    }

    public synchronized Map<String, Object> moveToken(String color, int tokenId) {
        color = normalizeColor(color);

        if (gameOver) {
            throw new IllegalStateException("Game is already over.");
        }
        if (!hasRolled || diceNumber == null) {
            throw new IllegalStateException("Roll the dice first.");
        }
        if (!color.equals(activePlayer)) {
            throw new IllegalArgumentException("It is not " + getPlayerName(activePlayer) + "'s turn.");
        }
        if (tokenId < 0 || tokenId >= 4) {
            throw new IllegalArgumentException("Invalid token ID (0-3).");
        }

        List<Integer> validIds = getValidTokenIds(activePlayer, diceNumber);
        if (!validIds.contains(tokenId)) {
            throw new IllegalArgumentException("This coin cannot move with the rolled dice value.");
        }

        return moveTokenInternal(color, tokenId, diceNumber);
    }

    private Map<String, Object> moveTokenInternal(String color, int tokenId, int rolledValue) {
        List<Integer> playerTokens = tokens.get(color);
        int currentPos = playerTokens.get(tokenId);

        // From Base to Start
        if (currentPos == -1) {
            playerTokens.set(tokenId, 0);
        } else {
            playerTokens.set(tokenId, currentPos + rolledValue);
        }

        int newPos = playerTokens.get(tokenId);
        boolean captured = false;

        // Check opponent capture (only valid on outer path: 0 to 50)
        if (newPos >= 0 && newPos <= 50) {
            captured = captureOpponents(color, newPos);
        }

        boolean finishedCoin = (newPos == 56);

        message = getPlayerName(color) + " moved coin " + (tokenId + 1) + ".";
        if (captured) message += " 💥 Opponent captured!";
        if (finishedCoin) message += " 🎯 Token reached home!";

        // Check if player has finished all 4 tokens
        if (hasPlayerFinished(color) && !placements.containsKey(color)) {
            int rank = placements.size() + 1;
            placements.put(color, rank);
            message += " 🏆 " + getPlayerName(color) + " secured " + rank + getRankSuffix(rank) + " Place!";
        }

        // Dynamic Game Over: When 3 out of 4 finished
        if (placements.size() == PLAYERS.size() - 1) {
            for (String p : PLAYERS) {
                if (!placements.containsKey(p)) {
                    placements.put(p, PLAYERS.size());
                    break;
                }
            }
            gameOver = true;
            activePlayer = null;
            hasRolled = false;
            consecutiveSixCount = 0;
            message += " 🏁 Game finished! All ranks finalized.";
            return getState();
        }

        // EXTRA TURN RULE: Rolled 6 OR Captured Opponent OR Token Reached Home!
        boolean bonusTurn = (rolledValue == 6 || captured || finishedCoin);
        hasRolled = false;

        if (bonusTurn && !hasPlayerFinished(color)) {
            activePlayer = color;
            if (finishedCoin) {
                message += " 🌟 Home bonus roll! " + getPlayerName(color) + " rolls again.";
            } else {
                message += " 🎲 Bonus turn! " + getPlayerName(color) + " rolls again.";
            }
        } else {
            consecutiveSixCount = 0;
            activePlayer = getNextPlayer(color);
            message += " " + getPlayerName(activePlayer) + "'s turn!";
        }

        return getState();
    }

    private boolean isValidMove(int currentPos, int dice) {
        if (currentPos == -1) {
            return dice == 6; // open only with 6
        }
        if (currentPos == 56) {
            return false; // already finished
        }
        return currentPos + dice <= 56; // Exact count required
    }

    private List<Integer> getValidTokenIds(String player, int dice) {
        List<Integer> validIds = new ArrayList<>();
        List<Integer> playerTokens = tokens.get(player);
        for (int i = 0; i < playerTokens.size(); i++) {
            if (isValidMove(playerTokens.get(i), dice)) {
                validIds.add(i);
            }
        }
        return validIds;
    }

    private boolean captureOpponents(String movingPlayer, int movingRelativePos) {
        int landingCell = getGlobalCell(movingPlayer, movingRelativePos);
        if (SAFE_CELLS.contains(landingCell)) {
            return false;
        }

        boolean captured = false;
        for (String opponent : PLAYERS) {
            if (opponent.equals(movingPlayer) || placements.containsKey(opponent)) {
                continue;
            }

            List<Integer> oppTokens = tokens.get(opponent);
            for (int i = 0; i < oppTokens.size(); i++) {
                int pos = oppTokens.get(i);
                if (pos >= 0 && pos <= 50) {
                    if (getGlobalCell(opponent, pos) == landingCell) {
                        oppTokens.set(i, -1); // send back to yard
                        captured = true;
                    }
                }
            }
        }
        return captured;
    }

    private int getGlobalCell(String color, int relativePos) {
        return (START_CELLS.get(color) + relativePos) % 52;
    }

    private String getNextPlayer(String currentPlayer) {
        int currIdx = PLAYERS.indexOf(currentPlayer);
        for (int i = 1; i <= PLAYERS.size(); i++) {
            String next = PLAYERS.get((currIdx + i) % PLAYERS.size());
            if (!placements.containsKey(next)) {
                return next;
            }
        }
        return currentPlayer;
    }

    private boolean hasPlayerFinished(String color) {
        List<Integer> playerTokens = tokens.get(color);
        for (int pos : playerTokens) {
            if (pos != 56) return false;
        }
        return true;
    }

    private String getPlayerName(String color) {
        if (color == null) return "";
        return playerNames.getOrDefault(color, color.toUpperCase());
    }

    private String normalizeColor(String color) {
        if (color == null) throw new IllegalArgumentException("Player color is required.");
        color = color.trim().toLowerCase();
        if (!PLAYERS.contains(color)) throw new IllegalArgumentException("Invalid color.");
        return color;
    }

    private String getRankSuffix(int rank) {
        return switch (rank) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
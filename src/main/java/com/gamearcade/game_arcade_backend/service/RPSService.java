package com.gamearcade.game_arcade_backend.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.gamearcade.game_arcade_backend.model.RPSRequest;
import com.gamearcade.game_arcade_backend.model.RPSResponse;

@Service
public class RPSService {

    public RPSResponse playGame(RPSRequest request) {

        String playerChoice = request.getPlayerChoice().toUpperCase();

        String[] choices = {"ROCK", "PAPER", "SCISSORS"};

        Random random = new Random();

        String computerChoice = choices[random.nextInt(choices.length)];

        String result;

        if (playerChoice.equals(computerChoice)) {
            result = "DRAW";
        }
        else if (
            (playerChoice.equals("ROCK") && computerChoice.equals("SCISSORS")) ||
            (playerChoice.equals("PAPER") && computerChoice.equals("ROCK")) ||
            (playerChoice.equals("SCISSORS") && computerChoice.equals("PAPER"))
        ) {
            result = "YOU WIN";
        }
        else {
            result = "COMPUTER WINS";
        }

        return new RPSResponse(
            playerChoice,
            computerChoice,
            result
        );
    }
}

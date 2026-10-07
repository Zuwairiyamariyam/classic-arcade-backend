package com.gamearcade.game_arcade_backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamearcade.game_arcade_backend.model.TicTacToeRequest;
import com.gamearcade.game_arcade_backend.model.TicTacToeResponse;
import com.gamearcade.game_arcade_backend.service.TicTacToeService;

@RestController
@RequestMapping("/api/tictactoe")
@CrossOrigin(origins = "*")
public class TicTacToeController {

    private final TicTacToeService ticTacToeService;

    public TicTacToeController(TicTacToeService ticTacToeService) {
        this.ticTacToeService = ticTacToeService;
    }

    @PostMapping("/play")
    public TicTacToeResponse playGame(@RequestBody TicTacToeRequest request) {
        return ticTacToeService.playGame(request);
    }
    
    @GetMapping("/reset")
    public TicTacToeResponse resetGame() {
        ticTacToeService.resetBoard();

        return new TicTacToeResponse(
            new String[][] {
                {"", "", ""},
                {"", "", ""},
                {"", "", ""}
            },
            "GAME STARTED",
            false
        );
    }
}
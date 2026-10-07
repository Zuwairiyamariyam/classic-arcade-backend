package com.gamearcade.game_arcade_backend.ludo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ludo")
@CrossOrigin(origins = "*")
public class LudoController {

    private final LudoService ludoService;

    public LudoController(LudoService ludoService) {
        this.ludoService = ludoService;
    }

    @PostMapping("/start")
    public Map<String, Object> startGame(@RequestBody(required = false) LudoStartRequest request) {
        Map<String, String> names = (request != null) ? request.getPlayerNames() : null;
        return ludoService.startGame(names);
    }

    @GetMapping("/state")
    public Map<String, Object> getState() {
        return ludoService.getState();
    }

    @PostMapping("/roll")
    public Map<String, Object> rollDice() {
        return ludoService.rollDice();
    }

    @PostMapping("/move")
    public Map<String, Object> moveToken(@RequestBody LudoMoveRequest request) {
        return ludoService.moveToken(request.getColor(), request.getTokenId());
    }

    @PostMapping("/reset")
    public Map<String, Object> resetGame() {
        return ludoService.resetGame();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleGameError(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
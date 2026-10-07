package com.gamearcade.game_arcade_backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamearcade.game_arcade_backend.model.RPSRequest;
import com.gamearcade.game_arcade_backend.model.RPSResponse;
import com.gamearcade.game_arcade_backend.service.RPSService;

@RestController
@RequestMapping("/api/rps")
@CrossOrigin(origins = "*")
public class RPSController {

    private final RPSService rpsService;

    public RPSController(RPSService rpsService) {
        this.rpsService = rpsService;
    }

    @PostMapping("/play")
    public RPSResponse playGame(@RequestBody RPSRequest request) {
        return rpsService.playGame(request);
    }
}
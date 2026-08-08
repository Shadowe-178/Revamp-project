package com.example.smartchess.controller.chess;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChessReplayController {

    @GetMapping("/chess/replay")
    public String replay() {
        return "replay";
    }
}

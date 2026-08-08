package com.example.smartchess.controller.chess;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ChessMoveController {

    @PostMapping("/chess/move")
    public String move() {
        return "chess";
    }
}

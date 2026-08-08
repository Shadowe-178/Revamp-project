package com.example.smartchess.controller.chess;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChessBoardController {

    @GetMapping("/chess/board")
    public String board() {
        return "chess";
    }
}

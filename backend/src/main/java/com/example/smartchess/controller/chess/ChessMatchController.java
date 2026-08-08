package com.example.smartchess.controller.chess;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ChessMatchController {

    @GetMapping("/chess")
    public String chessIndex() {
        // 提供新的 Vite 前端 static/index.html
        return "forward:/index.html";
    }
}

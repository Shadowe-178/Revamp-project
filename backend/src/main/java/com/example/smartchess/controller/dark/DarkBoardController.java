package com.example.smartchess.controller.dark;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DarkBoardController {

    @GetMapping("/dark/board")
    public String board() {
        return "dark";
    }
}

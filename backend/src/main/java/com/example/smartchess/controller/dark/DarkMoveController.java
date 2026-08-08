package com.example.smartchess.controller.dark;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DarkMoveController {

    @PostMapping("/dark/move")
    public String move() {
        return "dark";
    }
}

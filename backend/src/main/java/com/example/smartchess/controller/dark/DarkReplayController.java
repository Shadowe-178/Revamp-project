package com.example.smartchess.controller.dark;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DarkReplayController {

    @GetMapping("/dark/replay")
    public String replay() {
        return "replay";
    }
}

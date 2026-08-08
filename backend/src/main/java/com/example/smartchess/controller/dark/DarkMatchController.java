package com.example.smartchess.controller.dark;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DarkMatchController {

    @GetMapping("/dark")
    public String darkIndex() {
        // 提供新的 Vite 前端 static/index.html
        return "forward:/index.html";
    }
}

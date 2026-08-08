package com.example.smartchess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistoryController {

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("title", "歷史對局");
        return "history";
    }
}

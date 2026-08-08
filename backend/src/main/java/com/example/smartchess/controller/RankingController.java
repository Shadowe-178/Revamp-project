package com.example.smartchess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RankingController {

    @GetMapping("/ranking")
    public String ranking(Model model) {
        model.addAttribute("title", "排行榜");
        return "ranking";
    }
}

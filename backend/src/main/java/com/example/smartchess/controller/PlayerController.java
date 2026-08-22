package com.example.smartchess.controller;

import com.example.smartchess.dto.PlayerLoginDto;
import com.example.smartchess.dto.PlayerProfileDto;
import com.example.smartchess.dto.PlayerRegisterDto;
import com.example.smartchess.entity.Player;
import com.example.smartchess.service.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginDto", new PlayerLoginDto());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute PlayerLoginDto loginDto, HttpServletRequest request, Model model) {
        try {
            Player player = playerService.login(loginDto);
            request.getSession().setAttribute("user", player.getUsername());
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("loginDto", loginDto);
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new PlayerRegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute PlayerRegisterDto registerDto, Model model) {
        try {
            playerService.register(registerDto);
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registerDto", registerDto);
            return "register";
        }
    }

    @GetMapping("/")
public String index(Model model, HttpServletRequest request) {
    String username = (String) request.getSession().getAttribute("user");

    // 沒有登入就不能進入遊戲
    if (username == null) {
        return "redirect:/login";
    }

    model.addAttribute("title", "SmartChess");
    model.addAttribute("user", username);

    return "index";
}

    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }
        PlayerProfileDto profile = playerService.getProfile(username);
        model.addAttribute("title", "個人資料");
        model.addAttribute("profile", profile);
        return "profile";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }

    @GetMapping("/replay")
    public String globalReplay() {
        return "replay";
    }
}

package com.example.EcoRadar.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class LoginController {
    @GetMapping
    public String index(Model model) {
        return "login/login"; // subpasta login
    }

    @PostMapping
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        if (username.isBlank() || password.isBlank()) {
            return "redirect:/login?error";
        }

        session.setAttribute("usuarioLogado", username);
        return "redirect:/home";
    }
}

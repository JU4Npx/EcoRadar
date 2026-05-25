package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/perfil")
    public String profile(HttpSession session,
                          Model model) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "loggedUser",
                loggedUser
        );

        return "profile/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login?logout";
    }
}
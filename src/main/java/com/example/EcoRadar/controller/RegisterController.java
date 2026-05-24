package com.example.EcoRadar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @GetMapping("/register")
    public String showRegister() {
        return "register/register";
    }

    @PostMapping("/register")
    public String handleRegister(RedirectAttributes ra) {
        // TODO: persist user
        ra.addFlashAttribute("message", "Cadastro realizado. Faca login.");
        return "redirect:/login";
    }
}

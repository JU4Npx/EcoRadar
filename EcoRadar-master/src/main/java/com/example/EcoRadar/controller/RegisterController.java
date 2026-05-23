package com.example.EcoRadar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(RedirectAttributes ra) {
        // TODO: persist user
        ra.addFlashAttribute("message", "Cadastro realizado. Faça login.");
        return "redirect:/login";
    }

}

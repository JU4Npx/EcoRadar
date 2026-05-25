package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;


    @GetMapping("/login")
    public String loginPage() {

        return "login/login";
    }

    @PostMapping("/login")
    public String login(

            @RequestParam String email,
            @RequestParam String password,

            HttpSession session,

            Model model
    ) {

        User user =
                userService.authenticate(
                        email,
                        password
                );

        if(user == null) {

            model.addAttribute(
                    "error",
                    "Email ou senha inválidos"
            );

            return "login/login";
        }

        session.setAttribute(
                "loggedUser",
                user
        );

        return "redirect:/home";
    }
}
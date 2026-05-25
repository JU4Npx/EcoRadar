package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    /*
    |--------------------------------------------------------------------------
    | REGISTER PAGE
    |--------------------------------------------------------------------------
    */

    @GetMapping("/register")
    public String showRegister() {

        return "register/register";
    }

    /*
    |--------------------------------------------------------------------------
    | REGISTER USER
    |--------------------------------------------------------------------------
    */

    @PostMapping("/register")
    public String handleRegister(

            @RequestParam String email,
            @RequestParam String password,

            RedirectAttributes ra
    ) {

        if(userService.emailExists(email)) {

            ra.addFlashAttribute(
                    "error",
                    "Este email já está cadastrado."
            );

            return "redirect:/register";
        }

        User user = new User();

        user.setEmail(email);

        user.setPassword(password);

        userService.save(user);

        ra.addFlashAttribute(
                "message",
                "Cadastro realizado com sucesso."
        );

        return "redirect:/login";
    }
}
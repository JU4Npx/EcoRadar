package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegister() {

        return "register/register";
    }

    @PostMapping("/register")
    public String handleRegister(

            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,

            RedirectAttributes ra
    ) {

        if(!password.equals(confirmPassword)) {

            ra.addFlashAttribute(
                    "error",
                    "As senhas nao conferem."
            );

            return "redirect:/register";
        }

        if(userService.emailExists(email)) {

            ra.addFlashAttribute(
                    "error",
                    "Este email já está cadastrado."
            );

            return "redirect:/register";
        }

        User user = new User();

        user.setName(name.trim());
        user.setEmail(email);


        user.setPassword(
                passwordEncoder.encode(password)
        );

        userService.save(user);

        ra.addFlashAttribute(
                "message",
                "Cadastro realizado com sucesso."
        );

        return "redirect:/login";
    }
}

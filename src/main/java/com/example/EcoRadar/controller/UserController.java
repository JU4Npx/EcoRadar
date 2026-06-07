package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String listUsers(
            HttpSession session,
            Model model
    ) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {

            return "redirect:/login";
        }

        if (!loggedUser.hasPermission(
                Permission.MANAGE_USERS
        )) {

            return "redirect:/home";
        }

        model.addAttribute(
                "users",
                userService.findAll()
        );

        return "user/list";
    }

    @PostMapping("/users/make-admin/{id}")
    public String makeAdmin(
            @PathVariable Integer id,
            HttpSession session
    ) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {

            return "redirect:/login";
        }

        if (!loggedUser.hasPermission(
                Permission.GRANT_ADMIN
        )) {

            return "redirect:/home";
        }

        userService.makeAdmin(id);

        return "redirect:/users";
    }
}
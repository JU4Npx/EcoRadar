package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.UserType;
import com.example.EcoRadar.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    /*
    |--------------------------------------------------------------------------
    | LISTAR USUÁRIOS
    |--------------------------------------------------------------------------
    */

    @GetMapping
    public String listUsers(Model model,
                            HttpSession session) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        if(loggedUser == null) {
            return "redirect:/login";
        }

        if(loggedUser.getType() != UserType.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute(
                "users",
                service.findAll()
        );

        return "user/list";
    }


    @PostMapping("/{id}/make-admin")
    public String makeAdmin(@PathVariable Integer id,
                            HttpSession session) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        // NÃO LOGADO
        if(loggedUser == null) {
            return "redirect:/login";
        }

        // NÃO É ADMIN
        if(loggedUser.getType() != UserType.ADMIN) {
            return "redirect:/";
        }

        service.makeAdmin(id);

        return "redirect:/users";
    }
}
package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.UserType;
import com.example.EcoRadar.service.EventService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/eventos")
public class EventController {

    @Autowired
    private EventService service;

    /*
    |--------------------------------------------------------------------------
    | LISTAR EVENTOS
    |--------------------------------------------------------------------------
    */

    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "eventos",
                service.findAll()
        );

        return "event/lista";
    }

    @GetMapping("/novo")
    public String newEvent(Model model,
                           HttpSession session) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null) {
            return "redirect:/login";
        }

        if(user.getType() != UserType.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute(
                "evento",
                new Event()
        );

        return "registerEvent/registerEvent";
    }

    @PostMapping("/salvar")
    public String save(@ModelAttribute Event event,
                       HttpSession session) {

        User user =
                (User) session.getAttribute("loggedUser");

        // PROTEÇÃO BACKEND
        if(user == null || user.getType() != UserType.ADMIN) {
            return "redirect:/";
        }

        service.save(event);

        return "redirect:/eventos";
    }
}
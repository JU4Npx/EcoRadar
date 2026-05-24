package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/eventos")
public class EventController {

    @Autowired
    private EventService service;

    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "eventos",
                service.findAll()
        );

        return "event/lista";
    }

    @GetMapping("/novo")
    public String new(Model model) {

        model.addAttribute(
                "evento",
                new Event()
        );

        return "registerEvent/registerEvent";
    }

    @PostMapping("/salver")
    public String save(@ModelAttribute Event event) {

        service.save(event);

        return "redirect:/eventos";
    }
}
package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.EventStatus;
import com.example.EcoRadar.model.enums.UserType;
import com.example.EcoRadar.service.EventService;
import com.example.EcoRadar.service.GreenAreaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/eventos")
public class EventController {

    @Autowired
    private EventService service;

    @Autowired
    private GreenAreaService greenAreaService;

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

        Event event = new Event();
        event.setStatus(EventStatus.SCHEDULED);

        model.addAttribute("evento", event);
        model.addAttribute("areasVerdes", greenAreaService.findAll());
        model.addAttribute("statuses", EventStatus.values());

        return "events/createEvent";
    }

    @PostMapping("/salvar")
    public String save(@ModelAttribute Event event,
                       @RequestParam Integer greenAreaId,
                       HttpSession session,
                       RedirectAttributes ra) {

        User user =
                (User) session.getAttribute("loggedUser");

        // PROTEÇÃO BACKEND
        if(user == null || user.getType() != UserType.ADMIN) {
            return "redirect:/";
        }

        GreenArea greenArea = greenAreaService.findById(greenAreaId).orElse(null);

        if (greenArea == null) {
            ra.addFlashAttribute("error", "Area verde nao encontrada.");
            return "redirect:/eventos/novo";
        }

        event.setGreenArea(greenArea);

        service.save(event);

        ra.addFlashAttribute("message", "Evento criado com sucesso.");

        return "redirect:/eventos/novo";
    }
}

package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.EventStatus;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.service.EventService;
import com.example.EcoRadar.service.GreenAreaService;
import com.example.EcoRadar.model.enums.EventCategory;


import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        return "events/editEvent";
    }

    @GetMapping("/novo")
    public String newEvent(Model model,
                           HttpSession session) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null) {
            return "redirect:/login";
        }

        if(!user.hasPermission(
                Permission.CREATE_EVENT
        )) {
            return "redirect:/home";
        }

        Event event = new Event();
        event.setStatus(EventStatus.SCHEDULED);

        model.addAttribute("evento", event);
        model.addAttribute("areasVerdes", greenAreaService.findAll());
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("categories", EventCategory.values());


        return "events/createEvent";
    }

    @PostMapping("/salvar")
    public String save(@ModelAttribute Event event,
                       @RequestParam Integer greenAreaId,
                       HttpSession session,
                       RedirectAttributes ra) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null ||
                !user.hasPermission(
                        Permission.CREATE_EVENT
                )) {

            return "redirect:/home";
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

    @GetMapping("/editar")
    public String editList(Model model,
                           HttpSession session) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null) {
            return "redirect:/login";
        }

        if(!user.hasPermission(
                Permission.EDIT_EVENT
        )) {

            return "redirect:/home";
        }

        model.addAttribute("eventos", service.findAll());

        return "events/editEvent";
    }
    @GetMapping("/areas-verdes/{id}/eventos")
    public String eventsByGreenArea(
            @PathVariable Integer id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "Todos") String status,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            Model model,
            RedirectAttributes ra) {

        // buscar area
        GreenArea area = greenAreaService.findById(id).orElse(null);
        if (area == null) {
            ra.addFlashAttribute("error", "Área verde não encontrada.");
            return "redirect:/home";
        }

        // buscar eventos da area
        List<Event> events = service.findByGreenAreaId(id);

        // Filtrar por search (title ou description)
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            events = events.stream()
                    .filter(ev -> (ev.getTitle() != null && ev.getTitle().toLowerCase().contains(q))
                            || (ev.getDescription() != null && ev.getDescription().toLowerCase().contains(q)))
                    .toList();
        }

        // Filtrar por status (EventStatus enum)
        if (status != null && !"Todos".equalsIgnoreCase(status) && !status.isBlank()) {
            final String wanted = status.toUpperCase();
            events = events.stream()
                    .filter(ev -> ev.getStatus() != null && ev.getStatus().name().equalsIgnoreCase(wanted))
                    .toList();
        }

        // Ordenação
        if ("oldest".equalsIgnoreCase(sort)) {
            events = events.stream()
                    .sorted(Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        } else { // default recent (mais recentes primeiro)
            events = events.stream()
                    .sorted(Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }

        // Agrupar por mês/ano (ex.: "Junho 2026")
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pt", "BR"));
        Map<String, List<Event>> grouped = new LinkedHashMap<>();

        events.stream()
                .sorted(Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(ev -> {
                    LocalDateTime dt = ev.getStartDate() != null ? ev.getStartDate() : (ev.getEndDate() != null ? ev.getEndDate() : LocalDateTime.MIN);
                    String key = dt.equals(LocalDateTime.MIN) ? "Sem data" : monthFormatter.format(dt);
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(ev);
                });

        model.addAttribute("area", area);
        model.addAttribute("groupedEvents", grouped);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("statuses", com.example.EcoRadar.model.enums.EventStatus.values());
        model.addAttribute("categories", com.example.EcoRadar.model.enums.EventCategory.values());

        return "events/areaEvents";
    }


    @GetMapping("/editar/{id}")
    public String editForm(@PathVariable Integer id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes ra) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null) {
            return "redirect:/login";
        }

        if(!user.hasPermission(
                Permission.EDIT_EVENT
        )) {

            return "redirect:/home";
        }

        Event event = service.findById(id).orElse(null);

        if (event == null) {
            ra.addFlashAttribute("error", "Evento nao encontrado.");
            return "redirect:/eventos/editar";
        }

        model.addAttribute("evento", event);
        model.addAttribute("eventos", service.findAll());
        model.addAttribute("areasVerdes", greenAreaService.findAll());
        model.addAttribute("statuses", EventStatus.values());

        return "events/editEvent";
    }

    @PostMapping("/atualizar")
    public String update(@ModelAttribute Event event,
                         @RequestParam Integer greenAreaId,
                         HttpSession session,
                         RedirectAttributes ra) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null ||
                !user.hasPermission(
                        Permission.EDIT_EVENT
                )) {

            return "redirect:/home";
        }

        GreenArea greenArea = greenAreaService.findById(greenAreaId).orElse(null);

        if (greenArea == null) {
            ra.addFlashAttribute("error", "Area verde nao encontrada.");
            return "redirect:/eventos/editar";
        }

        if (event.getId() == null || service.update(event.getId(), event, greenArea).isEmpty()) {
            ra.addFlashAttribute("error", "Evento nao encontrado.");
            return "redirect:/eventos/editar";
        }

        ra.addFlashAttribute("message", "Evento atualizado com sucesso.");

        return "redirect:/eventos/editar";
    }

    @GetMapping("/remover")
    public String removeList(Model model,
                             HttpSession session) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null) {
            return "redirect:/login";
        }

        if(!user.hasPermission(
                Permission.DELETE_EVENT
        )) {

            return "redirect:/home";
        }

        model.addAttribute("eventos", service.findAll());

        return "events/removeEvent";
    }

    @PostMapping("/remover/{id}")
    public String remove(@PathVariable Integer id,
                         HttpSession session,
                         RedirectAttributes ra) {

        User user =
                (User) session.getAttribute("loggedUser");

        if(user == null ||
                !user.hasPermission(
                        Permission.DELETE_EVENT
                )) {

            return "redirect:/home";
        }

        if (service.findById(id).isEmpty()) {
            ra.addFlashAttribute("error", "Evento nao encontrado.");
            return "redirect:/eventos/remover";
        }

        service.delete(id);

        ra.addFlashAttribute("message", "Evento removido com sucesso.");

        return "redirect:/eventos/remover";
    }
}

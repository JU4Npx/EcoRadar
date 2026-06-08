package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.GreenAreaAddress;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.GreenAreaStatus;
import com.example.EcoRadar.model.enums.GreenAreaType;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.service.GreenAreaService;
import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.service.EventService;
import com.example.EcoRadar.model.enums.EventStatus;
import com.example.EcoRadar.model.enums.EventCategory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/areas-verdes")
public class GreenAreaController {

    @Autowired
    private GreenAreaService service;
    @Autowired
    private EventService eventService;


    @GetMapping("/nova")
    public String newGreenArea(
            Model model,
            HttpSession session
    ) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        if (!user.hasPermission(
                Permission.CREATE_GREEN_AREA
        )) {

            return "redirect:/home";
        }

        if (!model.containsAttribute("greenArea")) {

            GreenArea greenArea =
                    new GreenArea();

            greenArea.setAddress(
                    new GreenAreaAddress()
            );

            greenArea.setStatus(
                    GreenAreaStatus.ACTIVE
            );

            model.addAttribute(
                    "greenArea",
                    greenArea
            );
        }

        model.addAttribute(
                "types",
                GreenAreaType.values()
        );

        model.addAttribute(
                "statuses",
                GreenAreaStatus.values()
        );

        model.addAttribute(
                "pageTitle",
                "Adicionar área verde"
        );

        model.addAttribute(
                "formAction",
                "/areas-verdes/salvar"
        );

        model.addAttribute(
                "submitLabel",
                "Salvar"
        );

        model.addAttribute(
                "cancelUrl",
                "/home"
        );

        return "green areas/addGreenAreas";
    }
    @GetMapping("/{id}/eventos")
    public String eventsByGreenArea(
            @PathVariable Integer id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "Todos") String status,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            Model model,
            RedirectAttributes ra) {

        // buscar area
        GreenArea area = service.findById(id).orElse(null);
        if (area == null) {
            ra.addFlashAttribute("error", "Área verde não encontrada.");
            return "redirect:/home";
        }

        // buscar eventos da area (use the EventService)
        List<Event> events = eventService.findByGreenAreaId(id);

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
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("categories", EventCategory.values());

        return "events/areaEvents";
    }


    @GetMapping("/editar")
    public String editList(
            Model model,
            HttpSession session
    ) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        if (!user.hasPermission(
                Permission.EDIT_GREEN_AREA
        )) {

            return "redirect:/home";
        }

        model.addAttribute(
                "greenAreas",
                service.findAll()
        );

        return "green areas/editGreenAreas";
    }

    @GetMapping("/editar/{id}")
    public String editForm(
            @PathVariable Integer id,
            Model model,
            HttpSession session,
            RedirectAttributes ra
    ) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        if (!user.hasPermission(
                Permission.EDIT_GREEN_AREA
        )) {

            return "redirect:/home";
        }

        GreenArea greenArea =
                service.findById(id)
                        .orElse(null);

        if (greenArea == null) {

            ra.addFlashAttribute(
                    "error",
                    "Área verde não encontrada."
            );

            return "redirect:/areas-verdes/editar";
        }

        if (greenArea.getAddress() == null) {

            greenArea.setAddress(
                    new GreenAreaAddress()
            );
        }

        model.addAttribute(
                "greenArea",
                greenArea
        );

        model.addAttribute(
                "types",
                GreenAreaType.values()
        );

        model.addAttribute(
                "statuses",
                GreenAreaStatus.values()
        );

        model.addAttribute(
                "pageTitle",
                "Editar área verde"
        );

        model.addAttribute(
                "formAction",
                "/areas-verdes/atualizar"
        );

        model.addAttribute(
                "submitLabel",
                "Atualizar"
        );

        model.addAttribute(
                "cancelUrl",
                "/areas-verdes/editar"
        );

        return "green areas/addGreenAreas";
    }

    @PostMapping("/salvar")
    public String save(
            @ModelAttribute GreenArea greenArea,
            HttpSession session,
            RedirectAttributes ra
    ) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null ||
                !user.hasPermission(
                        Permission.CREATE_GREEN_AREA
                )) {

            return "redirect:/home";
        }

        try {

            service.save(greenArea);

        } catch (DataIntegrityViolationException e) {

            ra.addFlashAttribute(
                    "error",
                    "Não foi possível salvar. Verifique se os textos não ultrapassam o limite permitido."
            );

            return "redirect:/areas-verdes/nova";
        }

        ra.addFlashAttribute(
                "message",
                "Área verde cadastrada com sucesso."
        );

        return "redirect:/areas-verdes/nova";
    }

    @PostMapping("/atualizar")
    public String update(
            @ModelAttribute GreenArea greenArea,
            HttpSession session,
            RedirectAttributes ra
    ) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null ||
                !user.hasPermission(
                        Permission.EDIT_GREEN_AREA
                )) {

            return "redirect:/home";
        }

        try {

            if (greenArea.getId() == null ||
                    service.update(
                            greenArea.getId(),
                            greenArea
                    ).isEmpty()) {

                ra.addFlashAttribute(
                        "error",
                        "Área verde não encontrada."
                );

                return "redirect:/areas-verdes/editar";
            }

        } catch (DataIntegrityViolationException e) {

            ra.addFlashAttribute(
                    "error",
                    "Não foi possível atualizar. Verifique se os textos não ultrapassam o limite permitido."
            );

            return "redirect:/areas-verdes/editar";
        }

        ra.addFlashAttribute(
                "message",
                "Área verde atualizada com sucesso."
        );

        return "redirect:/areas-verdes/editar";
    }

    @GetMapping("/remover")
    public String removeList(
            Model model,
            HttpSession session
    ) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        if (!user.hasPermission(
                Permission.DELETE_GREEN_AREA
        )) {

            return "redirect:/home";
        }

        model.addAttribute(
                "greenAreas",
                service.findAll()
        );

        return "green areas/removeGreenAreas";
    }

    @PostMapping("/remover/{id}")
    public String remove(
            @PathVariable Integer id,
            HttpSession session,
            RedirectAttributes ra
    ) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null ||
                !user.hasPermission(
                        Permission.DELETE_GREEN_AREA
                )) {

            return "redirect:/home";
        }

        if (service.findById(id).isEmpty()) {

            ra.addFlashAttribute(
                    "error",
                    "Area verde nao encontrada."
            );

            return "redirect:/areas-verdes/remover";
        }

        try {

            service.delete(id);

        } catch (DataIntegrityViolationException e) {

            ra.addFlashAttribute(
                    "error",
                    "Nao foi possivel remover. Existem eventos ou favoritos vinculados a esta area verde."
            );

            return "redirect:/areas-verdes/remover";
        }

        ra.addFlashAttribute(
                "message",
                "Area verde removida com sucesso."
        );

        return "redirect:/areas-verdes/remover";
    }
}

package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.GreenAreaAddress;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.GreenAreaStatus;
import com.example.EcoRadar.model.enums.GreenAreaType;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.service.GreenAreaService;

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

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/areas-verdes")
public class GreenAreaController {

    @Autowired
    private GreenAreaService service;

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

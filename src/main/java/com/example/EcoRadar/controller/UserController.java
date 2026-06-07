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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class UserController {

    private static final List<Permission> MANAGED_ADMIN_PERMISSIONS =
            List.of(
                    Permission.MANAGE_USERS,
                    Permission.CREATE_GREEN_AREA,
                    Permission.EDIT_GREEN_AREA,
                    Permission.DELETE_GREEN_AREA,
                    Permission.CREATE_EVENT,
                    Permission.EDIT_EVENT,
                    Permission.DELETE_EVENT
            );

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

        model.addAttribute(
                "managedAdminPermissions",
                MANAGED_ADMIN_PERMISSIONS
        );

        model.addAttribute(
                "canEditAdminPermissions",
                userService.isMainAdmin(loggedUser.getId())
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

        if (!userService.isMainAdmin(
                loggedUser.getId()
        )) {

            return "redirect:/home";
        }

        userService.makeAdmin(id);

        return "redirect:/users";
    }

    @PostMapping("/users/remove-admin/{id}")
    public String removeAdmin(
            @PathVariable Integer id,
            HttpSession session
    ) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {

            return "redirect:/login";
        }

        if (!userService.isMainAdmin(
                loggedUser.getId()
        )) {

            return "redirect:/home";
        }

        userService.removeAdmin(id);

        return "redirect:/users";
    }

    @PostMapping("/users/{id}/permissions/{permission}")
    public String updatePermission(
            @PathVariable Integer id,
            @PathVariable Permission permission,
            @RequestParam(defaultValue = "false") boolean enabled,
            HttpSession session
    ) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {

            return "redirect:/login";
        }

        if (!userService.isMainAdmin(
                loggedUser.getId()
        )) {

            return "redirect:/home";
        }

        if (!MANAGED_ADMIN_PERMISSIONS.contains(permission)) {

            return "redirect:/users";
        }

        userService.setPermission(
                id,
                permission,
                enabled
        );

        return "redirect:/users";
    }
}

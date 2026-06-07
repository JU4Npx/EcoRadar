package com.example.EcoRadar.controller;

import com.example.EcoRadar.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LoggedUserControllerAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void refreshLoggedUser(
            HttpSession session
    ) {

        userService.refreshLoggedUser(session);
    }
}

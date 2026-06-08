package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class FavoritesController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/favorites")
    public String favorites(Model model, HttpSession session) {
        // Obter usuário logado
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            // Se não estiver logado, redirecionar para login
            return "redirect:/login";
        }

        // Buscar favoritos do usuário no banco
        List<GreenArea> favoritos = favoriteService.getFavoritesForUser(loggedUser);

        model.addAttribute("favoritos", favoritos);

        return "favorites/favorites";
    }
}

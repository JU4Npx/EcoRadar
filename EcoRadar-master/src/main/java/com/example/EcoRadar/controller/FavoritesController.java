package com.example.EcoRadar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class FavoritesController {

    @GetMapping("/favorites")
    public String favorites(Model model) {
        List<String> favoritos = Arrays.asList(
                "Praia de Pajuçara",
                "Parque Municipal de Maceió",
                "Feira de Artesanato",
                "Museu Théo Brandão"
        );

        model.addAttribute("favoritos", favoritos);
        return "favorites/favorites"; // corresponde a favorites.html
    }
}

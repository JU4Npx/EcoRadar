package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import com.example.EcoRadar.model.entity.GreenArea;

@RestController
@RequestMapping("/api/favorites")
public class GreenAreaFavoriteApiController {

    @Autowired
    private FavoriteService favoriteService;

    // POST /api/favorites/{areaId} - Adicionar favorito
    @PostMapping("/{areaId}")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @PathVariable Integer areaId,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuário não autenticado"));
        }

        favoriteService.addFavorite(user, areaId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Área adicionada aos favoritos");
        response.put("areaId", areaId);

        return ResponseEntity.ok(response);
    }

    // DELETE /api/favorites/{areaId} - Remover favorito
    @DeleteMapping("/{areaId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Integer areaId,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuário não autenticado"));
        }

        favoriteService.removeFavorite(user, areaId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Área removida dos favoritos");
        response.put("areaId", areaId);

        return ResponseEntity.ok(response);
    }

    // GET /api/favorites/status/{areaId} - Verificar se é favorita
    @GetMapping("/status/{areaId}")
    public ResponseEntity<Map<String, Object>> getFavoriteStatus(
            @PathVariable Integer areaId,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.ok(Map.of("isFavorite", false));
        }

        boolean isFavorite = favoriteService.isFavorite(user, areaId);

        Map<String, Object> response = new HashMap<>();
        response.put("areaId", areaId);
        response.put("isFavorite", isFavorite);

        return ResponseEntity.ok(response);
    }

    // GET /api/favorites - Lista de ids favoritas do usuário
    @GetMapping("")
    public ResponseEntity<List<Integer>> listFavorites(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }

        List<GreenArea> favs = favoriteService.getFavoritesForUser(user);
        List<Integer> ids = favs.stream().map(GreenArea::getId).collect(Collectors.toList());

        return ResponseEntity.ok(ids);
    }
}

package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.Favorite;
import com.example.EcoRadar.model.entity.FavoriteId;
import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.repository.FavoriteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private GreenAreaService greenAreaService;

    // Adicionar favorito
    @Transactional
    public Favorite addFavorite(User user, Integer greenAreaId) {
        Optional<GreenArea> optArea = greenAreaService.findById(greenAreaId);
        if (optArea.isEmpty()) {
            return null;
        }

        GreenArea greenArea = optArea.get();

        FavoriteId id = new FavoriteId();
        id.setIdUser(user.getId());
        id.setIdGreenArea(greenAreaId);

        Favorite favorite = new Favorite();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setGreenArea(greenArea);
        favorite.setFavoriteDate(LocalDateTime.now());
        favorite.setActive(true);
        favorite.setNotifications(true);

        return favoriteRepository.save(favorite);
    }

    // Remover favorito
    @Transactional
    public void removeFavorite(User user, Integer greenAreaId) {
        FavoriteId id = new FavoriteId();
        id.setIdUser(user.getId());
        id.setIdGreenArea(greenAreaId);

        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
        }
    }

    // Obter favoritos do usuário
    public List<GreenArea> getFavoritesForUser(User user) {
        List<Favorite> favorites = favoriteRepository.findByUser(user);
        return favorites.stream()
                .map(Favorite::getGreenArea)
                .collect(Collectors.toList());
    }

    // Verificar se área é favorita do usuário
    public boolean isFavorite(User user, Integer greenAreaId) {
        FavoriteId id = new FavoriteId();
        id.setIdUser(user.getId());
        id.setIdGreenArea(greenAreaId);

        return favoriteRepository.existsById(id);
    }
}

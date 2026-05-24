package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.Favorite;
import com.example.EcoRadar.model.entity.FavoriteId;
import com.example.EcoRadar.model.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository
        extends JpaRepository<Favorite, FavoriteId> {

    List<Favorite> findByUser(User user);
}
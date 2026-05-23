package com.example.EcoRadar.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
public class Favorite {

    @EmbeddedId
    private FavoriteId id;

    @ManyToOne
    @MapsId("idUser")
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne
    @MapsId("idGreenArea")
    @JoinColumn(name = "id_green_area")
    private GreenArea greenArea;

    @Column(name = "favorite_date")
    private LocalDateTime favoriteDate;

    @Column(name = "removed_date")
    private LocalDateTime removedDate;

    private Boolean active;

    private Boolean notifications;
}
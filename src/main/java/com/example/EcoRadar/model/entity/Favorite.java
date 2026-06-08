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

    // Construtores
    public Favorite() {}

    public Favorite(FavoriteId id, User user, GreenArea greenArea) {
        this.id = id;
        this.user = user;
        this.greenArea = greenArea;
    }

    // Getters e Setters
    public FavoriteId getId() {
        return id;
    }

    public void setId(FavoriteId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GreenArea getGreenArea() {
        return greenArea;
    }

    public void setGreenArea(GreenArea greenArea) {
        this.greenArea = greenArea;
    }

    public LocalDateTime getFavoriteDate() {
        return favoriteDate;
    }

    public void setFavoriteDate(LocalDateTime favoriteDate) {
        this.favoriteDate = favoriteDate;
    }

    public LocalDateTime getRemovedDate() {
        return removedDate;
    }

    public void setRemovedDate(LocalDateTime removedDate) {
        this.removedDate = removedDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getNotifications() {
        return notifications;
    }

    public void setNotifications(Boolean notifications) {
        this.notifications = notifications;
    }
}

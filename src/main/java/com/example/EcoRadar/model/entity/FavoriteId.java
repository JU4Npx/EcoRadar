package com.example.EcoRadar.model.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FavoriteId implements Serializable {

    private Integer idUser;
    private Integer idGreenArea;

    public FavoriteId() {}

    public FavoriteId(Integer idUser, Integer idGreenArea) {
        this.idUser = idUser;
        this.idGreenArea = idGreenArea;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public Integer getIdGreenArea() {
        return idGreenArea;
    }

    public void setIdGreenArea(Integer idGreenArea) {
        this.idGreenArea = idGreenArea;
    }

    @Override
    public int hashCode() {
        return (idUser != null ? idUser.hashCode() : 0) +
                (idGreenArea != null ? idGreenArea.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FavoriteId)) return false;
        FavoriteId other = (FavoriteId) obj;
        return idUser != null && idUser.equals(other.idUser) &&
                idGreenArea != null && idGreenArea.equals(other.idGreenArea);
    }
}

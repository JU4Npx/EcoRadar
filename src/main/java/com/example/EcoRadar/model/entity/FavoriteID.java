package com.example.EcoRadar.model.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FavoriteId implements Serializable {

    private Long idUser;

    private Long idGreenArea;
}
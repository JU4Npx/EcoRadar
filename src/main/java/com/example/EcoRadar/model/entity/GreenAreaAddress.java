package com.example.EcoRadar.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "green_area_addresses")
public class GreenAreaAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "green_area_address_id")
    private Integer id;

    private String cep;

    private String street;

    private String neighborhood;

    private String city;

    private String state;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @OneToOne
    @JoinColumn(name = "green_area_id")
    private GreenArea greenArea;
}
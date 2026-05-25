package com.example.EcoRadar.model.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import jakarta.persistence.Column;


@Entity
@Table(name = "green_area_addresses")
public class GreenAreaAddress extends Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "green_area_address_id")
    private Integer greenAreaAddressId;

    @OneToOne
    @JoinColumn(name = "green_area_id")
    private GreenArea greenArea;

    @Column(length = 50)
    private String state;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    public GreenAreaAddress() {
    }
}
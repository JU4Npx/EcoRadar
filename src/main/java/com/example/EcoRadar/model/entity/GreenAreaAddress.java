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

    public Integer getGreenAreaAddressId() {
        return greenAreaAddressId;
    }

    public void setGreenAreaAddressId(Integer greenAreaAddressId) {
        this.greenAreaAddressId = greenAreaAddressId;
    }

    public GreenArea getGreenArea() {
        return greenArea;
    }

    public void setGreenArea(GreenArea greenArea) {
        this.greenArea = greenArea;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}

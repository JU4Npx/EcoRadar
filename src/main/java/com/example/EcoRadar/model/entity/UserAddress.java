package com.example.EcoRadar.model.entity;


import jakarta.persistence.*;
import jakarta.persistence.Column;



@Entity
@Table(name = "user_addresses")
public class UserAddress extends Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_address_id")
    private Integer userAddressId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "house_number")
    private Integer houseNumber;

    public UserAddress() {
    }
}
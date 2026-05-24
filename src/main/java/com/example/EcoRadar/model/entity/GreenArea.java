package com.example.EcoRadar.model.entity;

import com.example.EcoRadar.model.enums.GreenAreaStatus;
import com.example.EcoRadar.model.enums.GreenAreaType;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "green_areas")
public class GreenArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "green_area_id")
    private Integer id;

    @Column(name = "green_area_name")
    private String name;

    @Enumerated(EnumType.STRING)
    private GreenAreaType type;

    private String description;

    @Enumerated(EnumType.STRING)
    private GreenAreaStatus status;

    @OneToOne(mappedBy = "greenArea", cascade = CascadeType.ALL)
    private GreenAreaAddress address;

    @OneToMany(mappedBy = "greenArea")
    private List<Event> events;
}
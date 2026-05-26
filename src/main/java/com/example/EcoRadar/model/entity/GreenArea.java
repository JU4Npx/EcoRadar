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

    public GreenArea() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GreenAreaType getType() {
        return type;
    }

    public void setType(GreenAreaType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GreenAreaStatus getStatus() {
        return status;
    }

    public void setStatus(GreenAreaStatus status) {
        this.status = status;
    }

    public GreenAreaAddress getAddress() {
        return address;
    }

    public void setAddress(GreenAreaAddress address) {
        this.address = address;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}

package com.example.EcoRadar.model.entity;

import com.example.EcoRadar.model.enums.EventStatus;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    private String title;

    private String description;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @ManyToOne
    @JoinColumn(name = "green_area_id")
    private GreenArea greenArea;
}
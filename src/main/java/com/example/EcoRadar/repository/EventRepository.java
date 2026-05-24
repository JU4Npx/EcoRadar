package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.model.entity.GreenArea;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository
        extends JpaRepository<Event, Integer> {

    List<Event>
    findByGreenArea(GreenArea greenArea);

    List<Event>
    findByStartDateAfter(LocalDateTime now);
}
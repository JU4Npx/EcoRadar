package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.repository.EventRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository repository;

    public Event save(Event event) {

        return repository.save(event);
    }

    public List<Event> findAll() {

        return repository.findAll();
    }

    public Optional<Event> findById(Integer id) {

        return repository.findById(id);
    }

    public List<Event> searchForGreenArea(
            GreenArea greenArea
    ) {

        return repository.findByGreenArea(
                greenArea
        );
    }

    public List<Event> findUpcomingEvents() {

        return repository.findByStartDateAfter(
                LocalDateTime.now()
        );
    }

    public void delete(Integer id) {

        repository.deleteById(id);
    }
}
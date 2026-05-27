package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.repository.EventRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository repository;

    @Transactional
    public Event save(Event event) {

        return repository.save(event);
    }

    @Transactional
    public Optional<Event> update(Integer id, Event formEvent, GreenArea greenArea) {

        return repository.findById(id)
                .map(event -> {
                    event.setTitle(formEvent.getTitle());
                    event.setDescription(formEvent.getDescription());
                    event.setStartDate(formEvent.getStartDate());
                    event.setEndDate(formEvent.getEndDate());
                    event.setStatus(formEvent.getStatus());
                    event.setGreenArea(greenArea);
                    return repository.save(event);
                });
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

    @Transactional
    public void delete(Integer id) {

        repository.deleteById(id);
    }
}

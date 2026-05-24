package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.enums.GreenAreaStatus;
import com.example.EcoRadar.repository.GreenAreaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GreenAreaService {

    @Autowired
    private GreenAreaRepository repository;

    public GreenArea save(GreenArea greenArea) {

        return repository.save(greenArea);
    }

    public List<GreenArea> findAll() {

        return repository.findAll();
    }

    public Optional<GreenArea> searchForId(Integer id) {

        return repository.findById(id);
    }

    public List<GreenArea> findAllActives() {

        return repository.findByStatus(
                GreenAreaStatus.ACTIVE
        );
    }

    public void delete(Integer id) {

        repository.deleteById(id);
    }
}
package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public User save(User user) {

        return repository.save(user);
    }

    public List<User> findAll() {

        return repository.findAll();
    }

    public Optional<User> searchForId(Integer id) {

        return repository.findById(id);
    }

    public void delete(Integer id) {

        repository.deleteById(id);
    }

    public boolean emailExists(String email) {

        return repository.existsByEmail(email);
    }
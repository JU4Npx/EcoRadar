package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.UserType;
import com.example.EcoRadar.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    /*
    |--------------------------------------------------------------------------
    | SAVE USER
    |--------------------------------------------------------------------------
    */

    public User save(User user) {

        user.setCreatedAt(
                LocalDateTime.now()
        );

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        return repository.save(user);
    }

    /*
    |--------------------------------------------------------------------------
    | UPDATE USER
    |--------------------------------------------------------------------------
    */

    public User update(User user) {

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        return repository.save(user);
    }

    /*
    |--------------------------------------------------------------------------
    | FIND ALL USERS
    |--------------------------------------------------------------------------
    */

    public List<User> findAll() {

        return repository.findAll();
    }

    /*
    |--------------------------------------------------------------------------
    | FIND USER BY ID
    |--------------------------------------------------------------------------
    */

    public Optional<User> findById(Integer id) {

        return repository.findById(id);
    }

    /*
    |--------------------------------------------------------------------------
    | FIND USER BY EMAIL
    |--------------------------------------------------------------------------
    */

    public Optional<User> findByEmail(String email) {

        return repository.findByEmail(email);
    }

    /*
    |--------------------------------------------------------------------------
    | DELETE USER
    |--------------------------------------------------------------------------
    */

    public void delete(Integer id) {

        repository.deleteById(id);
    }

    /*
    |--------------------------------------------------------------------------
    | CHECK IF EMAIL EXISTS
    |--------------------------------------------------------------------------
    */

    public boolean emailExists(String email) {

        return repository.existsByEmail(email);
    }

    /*
    |--------------------------------------------------------------------------
    | LOGIN AUTHENTICATION
    |--------------------------------------------------------------------------
    */

    public User authenticate(String email,
                             String password) {

        Optional<User> optionalUser =
                repository.findByEmail(email);

        if(optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        // LOGIN TEMPORÁRIO SEM BCrypt
        if(password.equals(user.getPassword())) {

            return user;
        }

        return null;
    }

    /*
    |--------------------------------------------------------------------------
    | MAKE ADMIN
    |--------------------------------------------------------------------------
    */

    public void makeAdmin(Integer userId) {

        Optional<User> optionalUser =
                repository.findById(userId);

        if(optionalUser.isPresent()) {

            User user =
                    optionalUser.get();

            user.setType(
                    UserType.ADMIN
            );

            repository.save(user);
        }
    }
}
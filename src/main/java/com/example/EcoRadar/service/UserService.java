package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.model.enums.UserType;
import com.example.EcoRadar.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user) {

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return repository.save(user);
    }

    public User update(User user) {

        user.setUpdatedAt(LocalDateTime.now());

        return repository.save(user);
    }

    public List<User> findAll() {

        return repository.findAll();
    }

    public Optional<User> findById(Integer id) {

        return repository.findById(id);
    }

    public User findByEmail(String email) {

        return repository
                .findByEmail(email)
                .orElse(null);
    }

    public void delete(Integer id) {

        if (id == 1) {
            return;
        }

        repository.deleteById(id);
    }

    public boolean emailExists(String email) {

        return repository.existsByEmail(email);
    }

    public User authenticate(
            String email,
            String password
    ) {

        Optional<User> optionalUser =
                repository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        if (passwordEncoder.matches(
                password,
                user.getPassword()
        )) {

            return user;
        }

        return null;
    }


    public void makeAdmin(Integer userId) {

        if (userId == 1) {
            return;
        }

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            user.setType(UserType.ADMIN);

            repository.save(user);
        }
    }


    public void removeAdmin(Integer userId) {

        if (userId == 1) {
            return;
        }

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            user.setType(UserType.USER);

            user.getPermissions().clear();

            repository.save(user);
        }
    }


    public void addPermission(
            Integer userId,
            Permission permission
    ) {

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            if (user.getType() != UserType.ADMIN) {
                return;
            }

            user.getPermissions()
                    .add(permission);

            repository.save(user);
        }
    }


    public void removePermission(
            Integer userId,
            Permission permission
    ) {

        if (userId == 1) {
            return;
        }

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            user.getPermissions()
                    .remove(permission);

            repository.save(user);
        }
    }

    public boolean isMainAdmin(
            Integer userId
    ) {

        return userId != null
                && userId.equals(1);
    }
}
package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.User;
import com.example.EcoRadar.model.enums.Permission;
import com.example.EcoRadar.model.enums.UserType;
import com.example.EcoRadar.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

    public User refreshLoggedUser(
            HttpSession session
    ) {

        Object loggedUserAttribute =
                session.getAttribute("loggedUser");

        if (!(loggedUserAttribute instanceof User)) {
            return null;
        }

        User loggedUser =
                (User) loggedUserAttribute;

        if (loggedUser.getId() == null) {
            return null;
        }

        Optional<User> refreshedUser =
                repository.findById(loggedUser.getId());

        if (refreshedUser.isEmpty()) {

            session.removeAttribute("loggedUser");
            return null;
        }

        session.setAttribute(
                "loggedUser",
                refreshedUser.get()
        );

        return refreshedUser.get();
    }

    public void delete(Integer id) {

        if (isMainAdmin(id)) {
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

        if (isMainAdmin(userId)) {
            return;
        }

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            if (user.getType() != UserType.ADMIN) {
                user.getPermissions().clear();
            }

            user.setType(UserType.ADMIN);
            user.setUpdatedAt(LocalDateTime.now());

            repository.save(user);
        }
    }

    public void removeAdmin(Integer userId) {

        if (isMainAdmin(userId)) {
            return;
        }

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            user.setType(UserType.USER);

            user.getPermissions().clear();

            user.setUpdatedAt(LocalDateTime.now());

            repository.save(user);
        }
    }

    public void addPermission(
            Integer userId,
            Permission permission
    ) {

        if (isMainAdmin(userId)) {
            return;
        }

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            if (user.getType() != UserType.ADMIN) {
                return;
            }

            user.getPermissions().add(permission);

            user.setUpdatedAt(LocalDateTime.now());

            repository.save(user);
        }
    }

    public void removePermission(
            Integer userId,
            Permission permission
    ) {

        if (isMainAdmin(userId)) {
            return;
        }

        Optional<User> optionalUser =
                repository.findById(userId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            user.getPermissions().remove(permission);

            user.setUpdatedAt(LocalDateTime.now());

            repository.save(user);
        }
    }

    public void setPermission(
            Integer userId,
            Permission permission,
            boolean enabled
    ) {

        if (enabled) {

            addPermission(
                    userId,
                    permission
            );

        } else {

            removePermission(
                    userId,
                    permission
            );
        }
    }

    public boolean isMainAdmin(
            Integer userId
    ) {

        return userId != null
                && userId.equals(1);
    }

    public boolean hasPermission(
            User user,
            Permission permission
    ) {

        if (user == null) {
            return false;
        }

        // Super Admin tem acesso total
        if (isMainAdmin(user.getId())) {
            return true;
        }

        if (user.getType() != UserType.ADMIN) {
            return false;
        }

        return user.getPermissions()
                .contains(permission);
    }

    public List<User> findAllFiltered(
            String sort,
            String search
    ) {

        String field = switch (sort) {

            case "name" -> "name";
            case "email" -> "email";
            case "type" -> "type";

            default -> "id";
        };

        List<User> users =
                repository.findAll(
                        Sort.by(field)
                );

        if (search == null ||
                search.isBlank()) {

            return users;
        }

        String filter =
                search.toLowerCase();

        return users.stream()
                .filter(user ->

                        (user.getName() != null &&
                                user.getName()
                                        .toLowerCase()
                                        .contains(filter))

                                ||

                                user.getEmail()
                                        .toLowerCase()
                                        .contains(filter)

                )
                .toList();
    }
}
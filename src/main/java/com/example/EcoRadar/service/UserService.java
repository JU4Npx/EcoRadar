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



    public User save(User user) {

        System.out.println("SALVANDO USUARIO");

        user.setCreatedAt(LocalDateTime.now());

        user.setUpdatedAt(LocalDateTime.now());

        return repository.save(user);
    }


    public User update(User user) {

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        return repository.save(user);
    }



    public List<User> findAll() {

        return repository.findAll();
    }



    public Optional<User> findById(Integer id) {

        return repository.findById(id);
    }



    public Optional<User> findByEmail(String email) {

        return repository.findByEmail(email);
    }



    public void delete(Integer id) {

        repository.deleteById(id);
    }



    public boolean emailExists(String email) {

        System.out.println("VERIFICANDO EMAIL: " + email);

        boolean exists =
                repository.findByEmail(email).isPresent();

        System.out.println("EMAIL EXISTE? " + exists);

        return exists;
    }



    public User authenticate(String email,
                             String password) {

        Optional<User> optionalUser =
                repository.findByEmail(email);

        if(optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();


        if(password.equals(user.getPassword())) {

            return user;
        }

        return null;
    }



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
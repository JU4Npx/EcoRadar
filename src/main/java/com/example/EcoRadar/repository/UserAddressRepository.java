package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository
        extends JpaRepository<UserAddress, Long> {
}
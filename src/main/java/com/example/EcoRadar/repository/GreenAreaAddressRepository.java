package com.example.EcoRadar.repository;

import com.example.EcoRadar.model.entity.GreenAreaAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreenAreaAddressRepository
        extends JpaRepository<GreenAreaAddress, Long> {
}
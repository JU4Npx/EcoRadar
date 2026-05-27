package com.example.EcoRadar.service;

import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.GreenAreaAddress;
import com.example.EcoRadar.model.enums.GreenAreaStatus;
import com.example.EcoRadar.repository.GreenAreaRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GreenAreaService {

    @Autowired
    private GreenAreaRepository repository;

    @Transactional
    public GreenArea save(GreenArea greenArea) {

        if (greenArea.getAddress() != null) {
            greenArea.getAddress().setGreenArea(greenArea);
        }

        return repository.save(greenArea);
    }

    @Transactional
    public Optional<GreenArea> update(Integer id, GreenArea formGreenArea) {

        return repository.findById(id)
                .map(greenArea -> {
                    greenArea.setName(formGreenArea.getName());
                    greenArea.setType(formGreenArea.getType());
                    greenArea.setDescription(formGreenArea.getDescription());
                    greenArea.setStatus(formGreenArea.getStatus());

                    GreenAreaAddress formAddress = formGreenArea.getAddress();
                    if (formAddress == null) {
                        greenArea.setAddress(null);
                        return repository.save(greenArea);
                    }

                    GreenAreaAddress address = greenArea.getAddress();
                    if (address == null) {
                        address = new GreenAreaAddress();
                        greenArea.setAddress(address);
                    }

                    address.setCep(formAddress.getCep());
                    address.setStreet(formAddress.getStreet());
                    address.setNeighborhood(formAddress.getNeighborhood());
                    address.setCity(formAddress.getCity());
                    address.setState(formAddress.getState());
                    address.setLatitude(formAddress.getLatitude());
                    address.setLongitude(formAddress.getLongitude());
                    address.setGreenArea(greenArea);

                    return repository.save(greenArea);
                });
    }

    public List<GreenArea> findAll() {

        return repository.findAll();
    }

    public Optional<GreenArea> findById(Integer id) {

        return repository.findById(id);
    }

    public List<GreenArea> findAllActives() {

        return repository.findByStatus(
                GreenAreaStatus.ACTIVE
        );
    }

    @Transactional
    public void delete(Integer id) {

        repository.deleteById(id);
    }
}

package com.space.repository;

import com.space.model.Ship;
import org.springframework.data.repository.CrudRepository;

public interface ShipRepo extends CrudRepository<Ship, Long> {
    Ship findAllById(Long id);
    void deleteById(Long id);
}

package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Cuisine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuisineRepository extends JpaRepository<Cuisine, String> {
}
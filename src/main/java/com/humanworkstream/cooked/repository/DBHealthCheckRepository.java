package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DBHealthCheckRepository extends JpaRepository<Recipe, Long> {

    @Query(value = "SELECT 1", nativeQuery = true)
    Integer checkDB();

    @Query(value = "SELECT COUNT(*) FROM app_user", nativeQuery = true)
    Integer checkSchema();
}
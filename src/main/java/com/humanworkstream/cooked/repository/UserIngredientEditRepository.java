package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.UserIngredientEdit;
import com.humanworkstream.cooked.entity.id.UserIngredientEditId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserIngredientEditRepository extends JpaRepository<UserIngredientEdit, UserIngredientEditId> {

    List<UserIngredientEdit> findByIdUserId(Long userId);
}
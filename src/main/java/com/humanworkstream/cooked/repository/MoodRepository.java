package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodRepository extends JpaRepository<Mood, String> {
}
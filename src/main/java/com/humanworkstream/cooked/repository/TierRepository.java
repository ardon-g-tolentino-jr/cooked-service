package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierRepository extends JpaRepository<Tier, String> {
}

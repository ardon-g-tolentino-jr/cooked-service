package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.TierLimit;
import com.humanworkstream.cooked.entity.id.TierLimitId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TierLimitRepository extends JpaRepository<TierLimit, TierLimitId> {

    Optional<TierLimit> findByIdTierAndIdComponent(String tier, String component);
}

package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @deprecated Use {@link AppUserRepository}.
 */
@Deprecated(forRemoval = true)
public interface UserRepository extends JpaRepository<AppUser, Long> {
}
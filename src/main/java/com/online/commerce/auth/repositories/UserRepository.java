package com.online.commerce.auth.repositories;

import com.online.commerce.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    User findByName(String name);

    boolean existsByEmail(String email);

}

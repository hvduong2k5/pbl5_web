package com.hvduong.detectiontomatoes.repository;

import com.hvduong.detectiontomatoes.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    
    Page<User> findByUsernameContainingIgnoreCase(String keyword, Pageable pageable);
}

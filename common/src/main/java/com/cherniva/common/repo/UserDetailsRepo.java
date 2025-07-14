package com.cherniva.common.repo;

import com.cherniva.common.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepo extends JpaRepository<UserDetails, Long> {
    Optional<UserDetails> findByUsername(String username);
}

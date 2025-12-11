package org.example.hrm.repository;

import org.example.hrm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserCode(String userCode);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUserCode(String userCode);
}

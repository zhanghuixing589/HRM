package org.example.hrm.repository;

import org.example.hrm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserCode(String userCode);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUserCode(String userCode);

    @Query("SELECT new map(u.userId as userId, u.userName as userName) FROM User u WHERE u.userId IN :userIds")
List<Map<String, Object>> findUserNamesByIds(@Param("userIds") List<Long> userIds);
}

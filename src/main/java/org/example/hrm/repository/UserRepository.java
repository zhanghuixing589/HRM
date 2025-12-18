package org.example.hrm.repository;

import org.example.hrm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Optional;

import javax.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findByUserCode(String userCode);
    
  Optional<User> findByEmail(String email);
    
  boolean existsByUserCode(String userCode);
    

    @Query("SELECT new map(u.userId as userId, u.userName as userName) FROM User u WHERE u.userId IN :userIds")
List<Map<String, Object>> findUserNamesByIds(@Param("userIds") List<Long> userIds);


  // 根据档案ID查询用户
  @Query("SELECT u FROM User u WHERE u.archiveId = :archiveId")
  Optional<User> findByArchiveId(@Param("archiveId") Long archiveId);
    
  // 查询最大工号（HRM开头）
  @Query("SELECT MAX(u.userCode) FROM User u WHERE u.userCode LIKE 'HRM%'")
  String findMaxUserCode();
  
  // 添加统计方法
  long countByStatus(Integer status);
  
  long countByStatusAndRoleType(Integer status, Integer roleType);

  // 更新用户状态和离职时间
  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.status = :status, u.leaveDate = :leaveDate, u.updateTime = CURRENT_TIMESTAMP WHERE u.archiveId = :archiveId")
  int updateUserStatusByArchiveId(@Param("archiveId") Long archiveId, 
                                  @Param("status") Integer status, 
                                  @Param("leaveDate") LocalDate leaveDate);
}


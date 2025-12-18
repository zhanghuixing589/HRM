package org.example.hrm.repository;

import java.util.List;
import java.util.Optional;

import org.example.hrm.entity.Standard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardRepository extends JpaRepository<Standard,Long>,JpaSpecificationExecutor<Standard> {

    // 优化查询：一次性加载所有关联数据
     @Query("SELECT s FROM Standard s " +
           "WHERE s.standardId = :standardId")
    Optional<Standard> findByIdWithAssociations(@Param("standardId") Long standardId);
    Optional<Standard> findByStandardCode(String standardCode);
    
    List<Standard> findByStatus(Integer status);

    // 查询某职位适用的标准
  @Query("SELECT DISTINCT s FROM Standard s " +
           "JOIN s.standardPositions sp " +
           "JOIN sp.position p " +
           "WHERE p.posId = :positionId AND s.status = 3")
    List<Standard> findActiveByPositionId(@Param("positionId") Integer positionId);

    
}

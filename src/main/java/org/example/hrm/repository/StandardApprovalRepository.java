package org.example.hrm.repository;

import java.util.List;
import java.util.Optional;

import org.example.hrm.dto.StandardDetailResponseDTO;
import org.example.hrm.dto.StandardDetailResponseDTO.ApprovalInfoDTO;
import org.example.hrm.entity.StandardApproval;
import org.example.hrm.entity.StandardApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardApprovalRepository  extends JpaRepository<StandardApproval,Long>{
  // 根据标准ID查找所有审核记录（按提交时间倒序）
    @Query("SELECT a FROM StandardApproval a WHERE a.standard.standardId = :standardId ORDER BY a.submitTime DESC")
    List<StandardApproval> findByStandardIdOrderBySubmitTimeDesc(@Param("standardId") Long standardId);
    
    // 查找当前活跃的审核记录（状态为待审核且是最新的）
    @Query("SELECT a FROM StandardApproval a WHERE a.standard.standardId = :standardId AND a.isActive = 1 ORDER BY a.submitTime DESC")
    List<StandardApproval> findActiveApprovalsByStandardId(@Param("standardId") Long standardId);
    
    // 查找当前活跃的审核记录（返回单个Optional）
    @Query("SELECT a FROM StandardApproval a WHERE a.standard.standardId = :standardId AND a.isActive = 1 ORDER BY a.submitTime DESC")
    Optional<StandardApproval> findActiveApproval(@Param("standardId") Long standardId);
    
    // 根据标准ID查找所有审核记录
    @Query("SELECT a FROM StandardApproval a WHERE a.standard.standardId = :standardId")
    List<StandardApproval> findByStandardId(@Param("standardId") Long standardId);

    
    }

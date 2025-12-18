package org.example.hrm.repository;

import java.util.List;

import org.example.hrm.entity.StandardApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardApprovalLogRepository extends JpaRepository<StandardApprovalLog,Long>{
    // 根据审核ID查找日志
    @Query("SELECT log FROM StandardApprovalLog log WHERE log.approvalId = :approvalId ORDER BY log.operationTime DESC")
    List<StandardApprovalLog> findByApprovalId(@Param("approvalId") Long approvalId);
    
    // 根据标准ID查找所有相关的审核日志
    @Query("SELECT log FROM StandardApprovalLog log " +
           "JOIN StandardApproval approval ON log.approvalId = approval.id " +
           "WHERE approval.standard.standardId = :standardId " +
           "ORDER BY log.operationTime DESC")
    List<StandardApprovalLog> findByStandardId(@Param("standardId") Long standardId);
    
    // 根据审核ID倒序排序（备用方法）
    @Query("SELECT log FROM StandardApprovalLog log WHERE log.approvalId = :approvalId ORDER BY log.operationTime DESC")
    List<StandardApprovalLog> findByApprovalIdOrderByOperationTimeDesc(@Param("approvalId") Long approvalId);
}

package org.example.hrm.repository;

import org.example.hrm.entity.ArchiveOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ArchiveOperationRepository extends JpaRepository<ArchiveOperation, Long> {
  List<ArchiveOperation> findByArcIdOrderByOperationTimeDesc(Long arcId);

  @Query("SELECT ao FROM ArchiveOperation ao WHERE ao.arcId = :arcId ORDER BY ao.operationTime DESC")
  List<ArchiveOperation> findArchiveProcess(@Param("arcId") Long arcId);

  // 获取最近一次操作记录
  @Query("SELECT ao FROM ArchiveOperation ao WHERE ao.arcId = :arcId ORDER BY ao.operationTime DESC")
  List<ArchiveOperation> findLatestOperation(@Param("arcId") Long arcId, Pageable pageable);

  // 获取特定操作类型的记录
  List<ArchiveOperation> findByArcIdAndOperationTypeOrderByOperationTimeDesc(Long arcId, Integer operationType);
}

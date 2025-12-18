package org.example.hrm.repository;

import org.example.hrm.entity.ChangeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChangeOperationRepository extends JpaRepository<ChangeOperation, Long> {

  List<ChangeOperation> findByChangeIdOrderByOperationTimeDesc(Long changeId);

  @Query("SELECT co FROM ChangeOperation co WHERE co.changeId = :changeId ORDER BY co.operationTime DESC")
  List<ChangeOperation> findChangeProcess(@Param("changeId") Long changeId);

  // 获取最近一次操作
  @Query("SELECT co FROM ChangeOperation co WHERE co.changeId = :changeId ORDER BY co.operationTime DESC")
  List<ChangeOperation> findLatestOperation(@Param("changeId") Long changeId);
}
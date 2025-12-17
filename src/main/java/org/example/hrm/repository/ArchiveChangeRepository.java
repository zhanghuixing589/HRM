package org.example.hrm.repository;

import org.example.hrm.entity.ArchiveChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveChangeRepository
    extends JpaRepository<ArchiveChange, Long>, JpaSpecificationExecutor<ArchiveChange> {

  Optional<ArchiveChange> findByChangeCode(String changeCode);

  List<ArchiveChange> findByArcId(Long arcId);

  List<ArchiveChange> findByStatus(Integer status);

  List<ArchiveChange> findByApplyId(Long applyId);

  List<ArchiveChange> findByReviewId(Long reviewId);

  List<ArchiveChange> findByArcIdAndStatus(Long arcId, Integer status);

  Page<ArchiveChange> findByStatus(Integer status, Pageable pageable);

  List<ArchiveChange> findByStatusIn(List<Integer> statuses);

  List<ArchiveChange> findByApplyIdAndStatus(Long applyId, Integer status);

  // 查询方法
  @Query("SELECT c FROM ArchiveChange c WHERE c.status IN (1, 4)")
  List<ArchiveChange> findPendingReviewChanges();

  // 获取最大变更编码
  @Query("SELECT MAX(c.changeCode) FROM ArchiveChange c WHERE YEAR(c.createTime) = YEAR(CURRENT_DATE)")
  String findMaxChangeCode();

  // 统计今年的变更数量
  @Query("SELECT COUNT(c) FROM ArchiveChange c WHERE YEAR(c.createTime) = YEAR(CURRENT_DATE)")
  Long countByYear();

  // 添加查询方法，用于按状态和档案ID查询
  @Query("SELECT c FROM ArchiveChange c WHERE c.arcId = :arcId AND c.status IN :statuses")
  List<ArchiveChange> findByArcIdAndStatusIn(@Param("arcId") Long arcId, @Param("statuses") List<Integer> statuses);
}
package org.example.hrm.repository;

import org.example.hrm.entity.Archive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveRepository extends JpaRepository<Archive, Long>, JpaSpecificationExecutor<Archive> {
  
  Optional<Archive> findByArcCode(String arcCode);
  List<Archive> findByWriteId(Long writeId);
  List<Archive> findByStatus(Integer status);
  
  @Query("SELECT a FROM Archive a WHERE a.thirdOrgId = :thirdOrgId")
  List<Archive> findByThirdOrgId(@Param("thirdOrgId") Long thirdOrgId);

  // 获取某个三级机构下的最新档案编码
  @Query("SELECT MAX(a.arcCode) FROM Archive a WHERE a.thirdOrgId = :thirdOrgId AND YEAR(a.createTime) = YEAR(CURRENT_DATE)")
  String findMaxArcCodeByThirdOrgId(@Param("thirdOrgId") Long thirdOrgId);

  // 统计某个三级机构下今年的档案数量
  @Query("SELECT COUNT(a) FROM Archive a WHERE a.thirdOrgId = :thirdOrgId AND YEAR(a.createTime) = YEAR(CURRENT_DATE)")
  Long countByThirdOrgIdAndYear(@Param("thirdOrgId") Long thirdOrgId);

  // 根据多个状态查询
  List<Archive> findByStatusIn(List<Integer> statuses);

  // 根据复核人查询 - 注意数据库字段名是 rewiewId
  // @Query("SELECT a FROM Archive a WHERE a.reviewId = :reviewId")
  // List<Archive> findByReviewId(@Param("reviewId") Long reviewId);
  List<Archive> findByReviewId(Long reviewId);  // 直接使用JPA方法名查询

  // 查询需要复核的档案（status为1或4）
  @Query("SELECT a FROM Archive a WHERE a.status IN (1, 4)")
  List<Archive> findPendingReviewArchives();

  // 根据复核人和状态查询
  List<Archive> findByReviewIdAndStatus(Long reviewId, Integer status);
}
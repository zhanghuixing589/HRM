// ArchiveStandardRepository.java
package org.example.hrm.repository;

import org.example.hrm.entity.ArchiveStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveStandardRepository extends JpaRepository<ArchiveStandard, Long> {

  Optional<ArchiveStandard> findByStandardCode(String standardCode);

  List<ArchiveStandard> findByStatus(Integer status);

  @Query("SELECT s FROM ArchiveStandard s WHERE s.status = 3") // 只查询已生效的
  List<ArchiveStandard> findActiveStandards();

  @Query("SELECT DISTINCT s FROM ArchiveStandard s " +
      "WHERE s.status = 3 AND (" +
      "s.standardName LIKE CONCAT('%', :keyword, '%') OR " +
      "s.standardCode LIKE CONCAT('%', :keyword, '%'))")
  List<ArchiveStandard> findByKeyword(@Param("keyword") String keyword);

  @Query("SELECT DISTINCT s FROM ArchiveStandard s " +
      "INNER JOIN ArchiveStandardPosition sp ON s.standardId = sp.standardId " +
      "WHERE sp.positionId = :positionId AND s.status = 3")
  List<ArchiveStandard> findByPositionId(@Param("positionId") Long positionId);
}
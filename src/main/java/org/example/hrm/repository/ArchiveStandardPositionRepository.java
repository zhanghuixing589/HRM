package org.example.hrm.repository;

import org.example.hrm.entity.ArchiveStandardPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchiveStandardPositionRepository extends JpaRepository<ArchiveStandardPosition, Long> {

  List<ArchiveStandardPosition> findByPositionId(Long positionId);

  List<ArchiveStandardPosition> findByStandardId(Long standardId);

  @Query("SELECT sp.positionId FROM ArchiveStandardPosition sp WHERE sp.standardId = :standardId")
  List<Long> findPositionIdsByStandardId(@Param("standardId") Long standardId);
}
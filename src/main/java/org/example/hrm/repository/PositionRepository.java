package org.example.hrm.repository;

import org.example.hrm.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

  Optional<Position> findByPosCode(String posCode);

  List<Position> findByOrgId(Long orgId);

  List<Position> findByStatus(Integer status);

  // 根据机构id和职位名称查询职位列表
  @Query("SELECT p FROM Position p WHERE p.orgId = :orgId AND p.posName = :posName")
  List<Position> findByOrgIdAndPosName(@Param("orgId") Long orgId, @Param("posName") String posName);

  // 根据机构id列表查询职位列表
  @Query("SELECT p FROM Position p WHERE p.orgId IN :orgIds")
  List<Position> findByOrgIds(@Param("orgIds") List<Long> orgIds);

  // 统计机构下的职位数量
  @Query("SELECT COUNT(p) FROM Position p WHERE p.orgId = :orgId AND p.status = 1")
  Long countByOrgId(@Param("orgId") Long orgId);

  // 分页查询根据机构ID
  Page<Position> findByOrgId(Long orgId, Pageable pageable);

  // 分页查询根据状态
  Page<Position> findByStatus(Integer status, Pageable pageable);

  // 分页查询根据机构ID列表
  @Query("SELECT p FROM Position p WHERE p.orgId IN :orgIds")
  Page<Position> findByOrgIds(@Param("orgIds") List<Long> orgIds, Pageable pageable);

  @Query("SELECT p FROM Position p WHERE p.posName LIKE %:keyword%")
  List<Position> findByPosNameLike(@Param("keyword") String keyword);
}

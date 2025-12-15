package org.example.hrm.repository;

import org.example.hrm.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

  Optional<Organization> findByOrgCode(String orgCode);

  List<Organization> findByParId(Long parId);

  List<Organization> findByOrgLevel(Integer orgLevel);

  List<Organization> findByStatus(Integer status);

  // 根据父级机构id和名称查询子机构列表
  @Query("SELECT o FROM Organization o WHERE o.parId = :parId AND o.orgName = :orgName")
  List<Organization> findByParIdAndOrgName(@Param("parId") Long parId, @Param("orgName") String orgName);

  // 根据机构级别和名称查询
  @Query("SELECT o FROM Organization o WHERE o.orgLevel = :level AND o.orgName = :orgName")
  List<Organization> findByOrgLevelAndOrgName(@Param("level") Integer level, @Param("orgName") String orgName);

  // 查询三级机构
  @Query("SELECT o FROM Organization o WHERE o.orgLevel = 3 AND o.orgId = :orgId")
  Optional<Organization> findThirdLevelOrgById(@Param("orgId") Long orgId);

  // 查询所有三级机构
  @Query("SELECT o FROM Organization o WHERE o.orgLevel = 3")
  List<Organization> findAllThirdLevelOrgs();

  // 根据级别查询最大机构编码
  @Query("SELECT MAX(o.orgCode) FROM Organization o WHERE o.orgLevel = :level")
  String findMaxOrgCodeByLevel(@Param("level") Integer level);

  @Query("SELECT o FROM Organization o WHERE o.orgCode = :orgCode AND (o.orgId != :excludeOrgId OR :excludeOrgId IS NULL)")
  Optional<Organization> findByOrgCodeAndExcludeId(@Param("orgCode") String orgCode,
      @Param("excludeOrgId") Long excludeOrgId);

  // 分页查询根据级别
  Page<Organization> findByOrgLevel(Integer orgLevel, Pageable pageable);

  // 根据父级ID分页查询
  Page<Organization> findByParId(Long parId, Pageable pageable);

  // 根据状态分页查询
  Page<Organization> findByStatus(Integer status, Pageable pageable);

  // 分页查询所有三级机构
  @Query("SELECT o FROM Organization o WHERE o.orgLevel = 3")
  Page<Organization> findAllThirdLevelOrgs(Pageable pageable);
}
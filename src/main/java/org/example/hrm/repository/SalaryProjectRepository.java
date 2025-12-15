package org.example.hrm.repository;

import java.util.List;
import java.util.Optional;

import org.example.hrm.entity.SalaryProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryProjectRepository extends JpaRepository<SalaryProject,Long> {

    /* 根据项目编码查询 */
    Optional<SalaryProject>findByProjectCode(String projectCode);

    /* 检查项目编码是否存在 */
    boolean existsByProjectCode(String projectCode);

    /* 根据项目类型查询 */
    List<SalaryProject> findByProjectType(Integer projectType);

    /* 根据项目类别查询 */
    List<SalaryProject> findByCategory(String category);

    /* 根据排序顺序排序查询 */
    List<SalaryProject> findAllByOrderBySortOrderAsc();

    
    
}

package org.example.hrm.repository;

import java.util.List;
import java.util.Optional;

import org.example.hrm.entity.SalaryProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * 根据状态查询项目
     */
    List<SalaryProject> findByStatus(Integer status);

     /**
     * 根据计算方式查询项目
     */
    List<SalaryProject> findByCalculationMethod(String calculationMethod);
    
    /**
     * 根据状态和项目类型查询
     */
    List<SalaryProject> findByStatusAndProjectType(Integer status, Integer projectType);
    
    /**
     * 查询所有启用的项目并按排序号排序
     */
    List<SalaryProject> findByStatusOrderBySortOrderAsc(Integer status);
    
    /**
     * 查询所有启用的收入项
     */
    @Query("SELECT p FROM SalaryProject p WHERE p.status = 1 AND p.projectType = 1 ORDER BY p.sortOrder")
    List<SalaryProject> findEnabledIncomeProjects();
    
    /**
     * 查询所有启用的扣减项
     */
    @Query("SELECT p FROM SalaryProject p WHERE p.status = 1 AND p.projectType = 2 ORDER BY p.sortOrder")
    List<SalaryProject> findEnabledDeductionProjects();
    
}

package org.example.hrm.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.example.hrm.common.BusinessException;
import org.example.hrm.common.ResultCode;
import org.example.hrm.entity.SalaryProject;
import org.example.hrm.repository.SalaryProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class SalaryProjectService {
    @Autowired SalaryProjectRepository salaryProjectRepository;

    /* 获取所有薪酬项目 */
    public List<SalaryProject>getAllProjects(){
        log.info("获取所有薪酬项目");
        return salaryProjectRepository.findAll();
    }

    /* 分页查询薪酬项目 */
    public Page<SalaryProject>getProjectsByPage(Pageable pageable){
        log.info("分页查询薪酬项目，页码：{}，页大小：{}",pageable.getPageNumber(),pageable.getPageSize());
        return salaryProjectRepository.findAll(pageable);
    }

    /* 根据ID获取薪酬项目 */
    public SalaryProject getProjectById(Long projectId){
        log.info("根据id获取薪酬项目：{}",projectId);
        Optional<SalaryProject> project = salaryProjectRepository.findById(projectId);
        return project.orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST,"薪酬项目不存在"));
    }

  /* 根据项目编码获取薪酬项目 */
  public SalaryProject getProjectByCode(String projectCode){
        log.info("根据项目编码获取薪酬项目：{}",projectCode);
        Optional<SalaryProject> project = salaryProjectRepository.findByProjectCode(projectCode);
        return project.orElse(null);
  }

  /* 创建新的薪酬项目 */
    public SalaryProject createProject(SalaryProject project){
        log.info("创建新的薪酬项目：{}",project.getProjectCode());
        
        // 检查项目编码是否已存在
        if (salaryProjectRepository.existsByProjectCode(project.getProjectCode())) {
            throw new BusinessException(ResultCode.DUPLICATE_KEY,"薪酬项目编码已存在");
        }

        //设置创建时间
        project.setCreatedAt(new Date());

        //设置排序
        if (project.getSortOrder() == null) {
            project.setSortOrder(0);
        }

        SalaryProject saveProject = salaryProjectRepository.save(project);
        log.info("薪酬项目创建成功：{}",saveProject.getProjectCode());
        return saveProject;
    }

    /* 更新薪酬项目 */
    public SalaryProject updateProject(Long projectId,SalaryProject project){
        log.info("更新薪酬项目：{}",projectId);
        
        //检查项目是否存在
        SalaryProject existingProject = getProjectById(projectId);

        //检查项目编码是否冲突
        if (!existingProject.getProjectCode().equals(project.getProjectCode())) {
            if (salaryProjectRepository.existsByProjectCode(project.getProjectCode())) {
                throw new BusinessException(ResultCode.DUPLICATE_KEY, "项目编码已存在");
            }
        }

        // 更新字段
        existingProject.setProjectCode(project.getProjectCode());
        existingProject.setProjectName(project.getProjectName());
        existingProject.setProjectType(project.getProjectType());
        existingProject.setCategory(project.getCategory());
        existingProject.setCalculationMethod(project.getCalculationMethod());
        existingProject.setSortOrder(project.getSortOrder());
        existingProject.setDescription(project.getDescription());
        
        SalaryProject updatedProject = salaryProjectRepository.save(existingProject);
        log.info("薪酬项目更新成功: {}", updatedProject.getProjectCode());
        return updatedProject;
    }

     /**
     * 删除薪酬项目
     */
    public void deleteProject(Long projectId) {
        log.info("删除薪酬项目: {}", projectId);
        
        // 检查项目是否存在
        if (!salaryProjectRepository.existsById(projectId)) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "薪酬项目不存在");
        }
        
        salaryProjectRepository.deleteById(projectId);
        log.info("薪酬项目删除成功: {}", projectId);
    }
    
    /**
     * 批量删除薪酬项目
     */
    public void batchDeleteProjects(List<Long> projectIds) {
        log.info("批量删除薪酬项目，数量: {}", projectIds.size());
        
        for (Long projectId : projectIds) {
            deleteProject(projectId);
        }
        
        log.info("批量删除完成");
    }
    
    /**
     * 根据项目类型查询
     */
    public List<SalaryProject> getProjectsByType(Integer projectType) {
        log.info("根据项目类型查询: {}", projectType);
        return salaryProjectRepository.findByProjectType(projectType);
    }
    
    /**
     * 根据类别查询
     */
    public List<SalaryProject> getProjectsByCategory(String category) {
        log.info("根据类别查询: {}", category);
        return salaryProjectRepository.findByCategory(category);
    }
    
    /**
     * 获取启用的薪酬项目列表
     */
    public List<SalaryProject> getEnabledProjects() {
        log.info("获取启用的薪酬项目列表");
        // 这里可以根据需要添加状态字段，当前所有项目都视为启用
        return salaryProjectRepository.findAll();
    }

}

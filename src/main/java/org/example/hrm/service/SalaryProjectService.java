package org.example.hrm.service;

import lombok.extern.slf4j.Slf4j;
import org.example.hrm.common.BusinessException;
import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.ProjectCreateDTO;
import org.example.hrm.dto.ProjectResponseDTO;
import org.example.hrm.dto.ProjectUpdateDTO;
import org.example.hrm.entity.SalaryProject;
import org.example.hrm.enums.SalaryEnums;
import org.example.hrm.repository.SalaryProjectRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SalaryProjectService {
    
    @Autowired
    private SalaryProjectRepository salaryProjectRepository;
    
    /**
     * 获取所有薪酬项目
     */
    public List<SalaryProject> getAllProjects() {
        log.info("获取所有薪酬项目");
        return salaryProjectRepository.findAll();
    }
    
    /**
     * 分页查询薪酬项目
     */
    public Page<SalaryProject> getProjectsByPage(Pageable pageable) {
        log.info("分页查询薪酬项目，页码：{}，页大小：{}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return salaryProjectRepository.findAll(pageable);
    }
    
    /**
     * 根据ID获取薪酬项目
     */
    public SalaryProject getProjectById(Long projectId) {
        log.info("根据ID获取薪酬项目：{}", projectId);
        Optional<SalaryProject> project = salaryProjectRepository.findById(projectId);
        return project.orElseThrow(() -> 
            new BusinessException(ResultCode.DATA_NOT_EXIST, "薪酬项目不存在")
        );
    }
    
    /**
     * 根据项目编码获取薪酬项目
     */
    public SalaryProject getProjectByCode(String projectCode) {
        log.info("根据项目编码获取薪酬项目：{}", projectCode);
        Optional<SalaryProject> project = salaryProjectRepository.findByProjectCode(projectCode);
        return project.orElseThrow(() -> 
            new BusinessException(ResultCode.DATA_NOT_EXIST, "薪酬项目不存在")
        );
    }
    
    /**
     * 创建新的薪酬项目
     */
    @Transactional
     public SalaryProject createProject(ProjectCreateDTO dto) {
        log.info("创建新的薪酬项目：{}", dto.getProjectCode());
        
        try {
            // 1. 验证项目类型
            if (dto.getProjectType() == null || (dto.getProjectType() != 1 && dto.getProjectType() != 2)) {
                log.error("项目类型验证失败: {}", dto.getProjectType());
                throw new BusinessException(ResultCode.PARAM_ERROR, "项目类型必须为1（增项）或2（减项）");
            }
            
            // 2. 检查项目编码是否已存在
            log.info("检查项目编码是否已存在: {}", dto.getProjectCode());
            boolean exists = salaryProjectRepository.existsByProjectCode(dto.getProjectCode());
            log.info("项目编码存在性检查结果: {}", exists);
            
            if (exists) {
                log.error("项目编码已存在: {}", dto.getProjectCode());
                throw new BusinessException(ResultCode.DUPLICATE_KEY, "薪酬项目编码已存在");
            }
            
            // 3. 转换DTO到实体
            SalaryProject project = new SalaryProject();
            BeanUtils.copyProperties(dto, project);
            
            // 4. 设置默认值
            if (project.getSortOrder() == null) {
                project.setSortOrder(0);
            }
            if (project.getStatus() == null) {
                project.setStatus(1);
            }
            
            // 5. 打印实体数据
            log.info("准备保存的实体数据: projectCode={}, projectName={}, projectType={}, category={}, calculationMethod={}, sortOrder={}, status={}",
                    project.getProjectCode(), project.getProjectName(), project.getProjectType(),
                    project.getCategory(), project.getCalculationMethod(), project.getSortOrder(), project.getStatus());
            
            // 6. 保存
            SalaryProject savedProject = salaryProjectRepository.save(project);
            log.info("薪酬项目创建成功，ID: {}, Code: {}", savedProject.getProjectId(), savedProject.getProjectCode());
            
            return savedProject;
            
        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("保存项目时发生异常: ", e);
            throw new BusinessException(ResultCode.ERROR, "保存项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新薪酬项目
     */
    public SalaryProject updateProject(Long projectId, ProjectUpdateDTO dto) {
        log.info("更新薪酬项目：{}", projectId);
        
        // 验证项目类型
        if (dto.getProjectType() == null || 
            (dto.getProjectType() != 1 && dto.getProjectType() != 2)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目类型必须为1（增项）或2（减项）");
        }
        
        // 检查项目是否存在
        SalaryProject existingProject = getProjectById(projectId);
        
        // 检查项目编码是否冲突（如果修改了编码）
        if (!existingProject.getProjectCode().equals(dto.getProjectCode())) {
            if (salaryProjectRepository.existsByProjectCode(dto.getProjectCode())) {
                throw new BusinessException(ResultCode.DUPLICATE_KEY, "项目编码已存在");
            }
        }
        
        // 更新字段
        existingProject.setProjectCode(dto.getProjectCode());
        existingProject.setProjectName(dto.getProjectName());
        existingProject.setProjectType(dto.getProjectType());
        existingProject.setCategory(dto.getCategory());
        existingProject.setCalculationMethod(dto.getCalculationMethod());
        existingProject.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        existingProject.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        existingProject.setDescription(dto.getDescription());
        existingProject.setParams(dto.getParams());
        
        SalaryProject updatedProject = salaryProjectRepository.save(existingProject);
        log.info("薪酬项目更新成功：{}", updatedProject.getProjectCode());
        
        return updatedProject;
    }
    
    /**
     * 删除薪酬项目
     */
    public void deleteProject(Long projectId) {
        log.info("删除薪酬项目：{}", projectId);
        
        // 检查项目是否存在
        if (!salaryProjectRepository.existsById(projectId)) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "薪酬项目不存在");
        }
        
        salaryProjectRepository.deleteById(projectId);
        log.info("薪酬项目删除成功：{}", projectId);
    }
    
    /**
     * 批量删除薪酬项目
     */
    public void batchDeleteProjects(List<Long> projectIds) {
        log.info("批量删除薪酬项目，数量：{}", projectIds.size());
        
        for (Long projectId : projectIds) {
            deleteProject(projectId);
        }
        
        log.info("批量删除完成");
    }
    
    /**
     * 根据项目类型查询项目
     */
    public List<SalaryProject> getProjectsByType(Integer projectType) {
        log.info("根据项目类型查询：{}", projectType);
        return salaryProjectRepository.findByProjectType(projectType);
    }
    
    /**
     * 根据类别查询项目
     */
    public List<SalaryProject> getProjectsByCategory(String category) {
        log.info("根据类别查询：{}", category);
        return salaryProjectRepository.findByCategory(category);
    }
    
    /**
     * 根据状态查询项目
     */
    public List<SalaryProject> getProjectsByStatus(Integer status) {
        log.info("根据状态查询：{}", status);
        return salaryProjectRepository.findByStatus(status);
    }
    
    /**
     * 查询所有启用的项目
     */
    public List<SalaryProject> getEnabledProjects() {
        log.info("获取所有启用的薪酬项目");
        return salaryProjectRepository.findByStatus(1);
    }
    
    /**
     * 查询所有启用的增项
     */
    public List<SalaryProject> getEnabledIncomeProjects() {
        log.info("获取所有启用的增项");
        return salaryProjectRepository.findByStatusAndProjectType(1, 1);
    }
    
    /**
     * 查询所有启用的减项
     */
    public List<SalaryProject> getEnabledDeductionProjects() {
        log.info("获取所有启用的减项");
        return salaryProjectRepository.findByStatusAndProjectType(1, 2);
    }
    
    /**
     * 启用项目
     */
    public void enableProject(Long projectId) {
        log.info("启用项目：{}", projectId);
        SalaryProject project = getProjectById(projectId);
        project.setStatus(1);
        salaryProjectRepository.save(project);
    }
    
    /**
     * 禁用项目
     */
    public void disableProject(Long projectId) {
        log.info("禁用项目：{}", projectId);
        SalaryProject project = getProjectById(projectId);
        project.setStatus(0);
        salaryProjectRepository.save(project);
    }
    
    /**
     * 检查项目编码是否存在
     */
    public boolean checkProjectCodeExists(String projectCode, Long excludeId) {
        log.info("检查项目编码是否存在：{}，排除ID：{}", projectCode, excludeId);
        
        Optional<SalaryProject> project = salaryProjectRepository.findByProjectCode(projectCode);
        
        if (project.isPresent()) {
            if (excludeId != null) {
                return !project.get().getProjectId().equals(excludeId);
            } else {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 转换实体到响应DTO
     */
    public ProjectResponseDTO convertToResponseDto(SalaryProject project) {
        if (project == null) {
            return null;
        }
        
        ProjectResponseDTO dto = new ProjectResponseDTO();
        BeanUtils.copyProperties(project, dto);
        dto.setProjectId(project.getProjectId());
        
        // 设置项目类型名称
        dto.setProjectTypeName(getProjectTypeName(project.getProjectType()));
        
        // 设置状态名称
        if (project.getStatus() != null) {
            dto.setStatusName(project.getStatus() == 1 ? "启用" : "禁用");
        }
        
        return dto;
    }
    
    /**
     * 获取项目类型名称
     */
    private String getProjectTypeName(Integer projectType) {
        if (projectType == null) return "未知";
        
        switch (projectType) {
            case 1: return "增项";
            case 2: return "减项";
            default: return "未知";
        }
    }
    
    /**
     * 批量转换实体到响应DTO
     */
    public List<ProjectResponseDTO> convertToResponseDtoList(List<SalaryProject> projects) {
        return projects.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取响应DTO
     */
    public ProjectResponseDTO getProjectDtoById(Long projectId) {
        SalaryProject project = getProjectById(projectId);
        return convertToResponseDto(project);
    }
    
    /**
     * 获取预定义的项目编码列表
     */
    public List<Map<String, Object>> getPredefinedProjectCodes() {
        return SalaryEnums.ProjectCode.toList();
    }
    
    /**
     * 验证项目类型
     */
    public boolean isValidProjectType(Integer projectType) {
        return projectType != null && (projectType == 1 || projectType == 2);
    }
    
    /**
     * 验证项目状态
     */
    public boolean isValidStatus(Integer status) {
        return status != null && (status == 0 || status == 1);
    }
}
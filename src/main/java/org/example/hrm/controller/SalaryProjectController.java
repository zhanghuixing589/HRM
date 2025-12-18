package org.example.hrm.controller;

import lombok.extern.slf4j.Slf4j;

import org.example.hrm.common.BusinessException;
import org.example.hrm.common.PageResult;
import org.example.hrm.common.Result;
import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.ProjectCreateDTO;
import org.example.hrm.dto.ProjectResponseDTO;
import org.example.hrm.dto.ProjectUpdateDTO;
import org.example.hrm.entity.SalaryProject;
import org.example.hrm.enums.SalaryEnums;
import org.example.hrm.service.SalaryProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salary/projects")
@CrossOrigin(origins = "*")
@Slf4j
@Validated
public class SalaryProjectController {
    
    @Autowired
    private SalaryProjectService salaryProjectService;
    
    /**
     * 获取所有薪酬项目
     */
    @GetMapping("/list")
    public Result<List<ProjectResponseDTO>> getAllProjects() {
        log.info("获取所有薪酬项目");
        try {
            List<SalaryProject> projects = salaryProjectService.getAllProjects();
            List<ProjectResponseDTO> responseList = salaryProjectService.convertToResponseDtoList(projects);
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取所有项目失败", e);
            return Result.error(ResultCode.ERROR, "获取项目列表失败");
        }
    }
    
    /**
     * 分页查询薪酬项目
     */
    @GetMapping("/page")
    public Result<PageResult<ProjectResponseDTO>> getProjectsByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Integer projectType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        
        log.info("分页查询薪酬项目，页码：{}，页大小：{}，项目编码：{}，项目名称：{}，项目类型：{}，类别：{}，状态：{}",
                 pageNum, pageSize, projectCode, projectName, projectType, category, status);
        
        try {
            // 创建分页请求
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize, 
                    Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.desc("createdAt")));
            
            // TODO: 这里可以添加条件过滤，暂时查询所有
            Page<SalaryProject> page = salaryProjectService.getProjectsByPage(pageable);
            
            // 转换为响应DTO
            List<ProjectResponseDTO> dtoList = salaryProjectService.convertToResponseDtoList(page.getContent());
            
            // 构建分页结果
            PageResult<ProjectResponseDTO> pageResult = new PageResult<>();
            pageResult.setPageNum(pageNum);
            pageResult.setPageSize(pageSize);
            pageResult.setTotal(page.getTotalElements());
            pageResult.setTotalPages(page.getTotalPages());
            pageResult.setList(dtoList);
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询失败", e);
            return Result.error(ResultCode.ERROR, "分页查询失败");
        }
    }
    
    /**
     * 根据ID获取薪酬项目
     */
    @GetMapping("/{id}")
    public Result<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        log.info("根据ID获取薪酬项目：{}", id);
        try {
            ProjectResponseDTO project = salaryProjectService.getProjectDtoById(id);
            return Result.success(project);
        } catch (Exception e) {
            log.error("获取项目失败，ID：{}", id, e);
            return Result.error(ResultCode.DATA_NOT_EXIST, "项目不存在");
        }
    }
    
    /**
     * 根据编码获取薪酬项目
     */
    @GetMapping("/code/{projectCode}")
    public Result<ProjectResponseDTO> getProjectByCode(@PathVariable String projectCode) {
        log.info("根据编码获取薪酬项目：{}", projectCode);
        try {
            SalaryProject project = salaryProjectService.getProjectByCode(projectCode);
            ProjectResponseDTO responseDTO = salaryProjectService.convertToResponseDto(project);
            return Result.success(responseDTO);
        } catch (Exception e) {
            log.error("获取项目失败，编码：{}", projectCode, e);
            return Result.error(ResultCode.DATA_NOT_EXIST, "项目不存在");
        }
    }
    
    /**
 * 创建新的薪酬项目
 */
@PostMapping("/create")
public Result<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectCreateDTO projectDTO) {
    log.info("创建新的薪酬项目开始，项目编码: {}", projectDTO.getProjectCode());
    
    try {
        // 打印接收到的请求体
        log.info("接收到的请求体: projectCode={}, projectName={}, projectType={}, category={}, calculationMethod={}, sortOrder={}, status={}",
                projectDTO.getProjectCode(), projectDTO.getProjectName(), projectDTO.getProjectType(),
                projectDTO.getCategory(), projectDTO.getCalculationMethod(), projectDTO.getSortOrder(), projectDTO.getStatus());
        
        // 验证项目类型
        if (projectDTO.getProjectType() == null || 
            (projectDTO.getProjectType() != 1 && projectDTO.getProjectType() != 2)) {
            log.error("项目类型验证失败: {}", projectDTO.getProjectType());
            return Result.error(ResultCode.PARAM_ERROR, "项目类型必须为1（增项）或2（减项）");
        }
        
        
        SalaryProject createdProject = salaryProjectService.createProject(projectDTO);
        ProjectResponseDTO responseDTO = salaryProjectService.convertToResponseDto(createdProject);
        
       
        return Result.success(ResultCode.SUCCESS, responseDTO);
        
    } catch (BusinessException e) {
        log.error("创建项目业务异常: {}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("创建项目异常: ", e);
        return Result.error(ResultCode.ERROR, "创建项目失败: " + e.getMessage());
    }
}
    
    /**
     * 更新薪酬项目
     */
    @PutMapping("/{id}")
    public Result<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateDTO projectDTO) {
        log.info("更新薪酬项目：{}", id);
        
        try {
            // 验证项目类型
            if (!salaryProjectService.isValidProjectType(projectDTO.getProjectType())) {
                return Result.error(ResultCode.PARAM_ERROR, "项目类型必须为1（增项）或2（减项）");
            }
            
            SalaryProject updatedProject = salaryProjectService.updateProject(id, projectDTO);
            ProjectResponseDTO responseDTO = salaryProjectService.convertToResponseDto(updatedProject);
            return Result.success(ResultCode.SUCCESS, responseDTO);
        } catch (BusinessException e) {
            log.error("更新项目失败：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新项目失败：{}", e.getMessage());
            return Result.error(ResultCode.ERROR, "更新项目失败");
        }
    }
    
    /**
     * 删除薪酬项目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        log.info("删除薪酬项目：{}", id);
        
        try {
            salaryProjectService.deleteProject(id);
            return Result.success();
        } catch (BusinessException e) {
            log.error("删除项目失败：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除项目失败：{}", e.getMessage());
            return Result.error(ResultCode.ERROR, "删除项目失败");
        }
    }
    
    /**
     * 批量删除薪酬项目
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteProjects(@RequestBody List<Long> projectIds) {
        log.info("批量删除薪酬项目，数量：{}", projectIds.size());
        
        try {
            salaryProjectService.batchDeleteProjects(projectIds);
            return Result.success();
        } catch (BusinessException e) {
            log.error("批量删除失败：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量删除失败：{}", e.getMessage());
            return Result.error(ResultCode.ERROR, "批量删除失败");
        }
    }
    
    /**
     * 启用项目
     */
    @PutMapping("/{id}/enable")
    public Result<Void> enableProject(@PathVariable Long id) {
        log.info("启用项目：{}", id);
        
        try {
            salaryProjectService.enableProject(id);
            return Result.success();
        } catch (BusinessException e) {
            log.error("启用项目失败：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("启用项目失败：{}", e.getMessage());
            return Result.error(ResultCode.ERROR, "启用项目失败");
        }
    }
    
    /**
     * 禁用项目
     */
    @PutMapping("/{id}/disable")
    public Result<Void> disableProject(@PathVariable Long id) {
        log.info("禁用项目：{}", id);
        
        try {
            salaryProjectService.disableProject(id);
            return Result.success();
        } catch (BusinessException e) {
            log.error("禁用项目失败：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("禁用项目失败：{}", e.getMessage());
            return Result.error(ResultCode.ERROR, "禁用项目失败");
        }
    }
    
    /**
     * 检查项目编码是否存在
     */
    @GetMapping("/check-code/{projectCode}")
    public Result<Map<String, Object>> checkProjectCode(
            @PathVariable String projectCode,
            @RequestParam(required = false) Long excludeId) {
        
        log.info("检查项目编码是否存在：{}，排除ID：{}", projectCode, excludeId);
        
        try {
            boolean exists = salaryProjectService.checkProjectCodeExists(projectCode, excludeId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("projectCode", projectCode);
            result.put("exists", exists);
            result.put("available", !exists);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查项目编码失败", e);
            return Result.error(ResultCode.ERROR, "检查失败");
        }
    }
    
    /**
     * 获取启用的薪酬项目列表
     */
    @GetMapping("/enabled")
    public Result<List<ProjectResponseDTO>> getEnabledProjects() {
        log.info("获取所有启用的薪酬项目");
        
        try {
            List<SalaryProject> projects = salaryProjectService.getEnabledProjects();
            List<ProjectResponseDTO> responseList = salaryProjectService.convertToResponseDtoList(projects);
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取启用项目失败", e);
            return Result.error(ResultCode.ERROR, "获取失败");
        }
    }
    
    /**
     * 获取启用的增项
     */
    @GetMapping("/enabled/income")
    public Result<List<ProjectResponseDTO>> getEnabledIncomeProjects() {
        log.info("获取所有启用的增项");
        
        try {
            List<SalaryProject> projects = salaryProjectService.getEnabledIncomeProjects();
            List<ProjectResponseDTO> responseList = salaryProjectService.convertToResponseDtoList(projects);
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取启用增项失败", e);
            return Result.error(ResultCode.ERROR, "获取失败");
        }
    }
    
    /**
     * 获取启用的减项
     */
    @GetMapping("/enabled/deduction")
    public Result<List<ProjectResponseDTO>> getEnabledDeductionProjects() {
        log.info("获取所有启用的减项");
        
        try {
            List<SalaryProject> projects = salaryProjectService.getEnabledDeductionProjects();
            List<ProjectResponseDTO> responseList = salaryProjectService.convertToResponseDtoList(projects);
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取启用减项失败", e);
            return Result.error(ResultCode.ERROR, "获取失败");
        }
    }
    
    /**
     * 根据类别查询项目
     */
    @GetMapping("/by-category/{category}")
    public Result<List<ProjectResponseDTO>> getProjectsByCategory(@PathVariable String category) {
        log.info("根据类别查询项目：{}", category);
        
        try {
            List<SalaryProject> projects = salaryProjectService.getProjectsByCategory(category);
            List<ProjectResponseDTO> responseList = salaryProjectService.convertToResponseDtoList(projects);
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("根据类别查询失败", e);
            return Result.error(ResultCode.ERROR, "查询失败");
        }
    }
    
    /**
     * 根据类型查询项目
     */
    @GetMapping("/by-type/{type}")
    public Result<List<ProjectResponseDTO>> getProjectsByType(@PathVariable Integer type) {
        log.info("根据类型查询项目：{}", type);
        
        try {
            // 验证类型
            if (type != 1 && type != 2) {
                return Result.error(ResultCode.PARAM_ERROR, "项目类型必须为1（增项）或2（减项）");
            }
            
            List<SalaryProject> projects = salaryProjectService.getProjectsByType(type);
            List<ProjectResponseDTO> responseList = salaryProjectService.convertToResponseDtoList(projects);
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("根据类型查询失败", e);
            return Result.error(ResultCode.ERROR, "查询失败");
        }
    }
    
    /**
     * 获取枚举值
     */
    @GetMapping("/enums")
    public Result<Map<String, Object>> getEnums() {
        log.info("获取薪酬项目管理相关的枚举值");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 使用现有的枚举类
            result.put("projectTypes", SalaryEnums.ProjectType.toList());
            result.put("projectCategories", SalaryEnums.ProjectCategory.toList());
            result.put("calculationMethods", SalaryEnums.CalculationMethod.toList());
            result.put("predefinedProjectCodes", SalaryEnums.ProjectCode.toList());
            
            // 项目状态选项
            Map<String, Object> statusOptions = new HashMap<>();
            statusOptions.put("1", "启用");
            statusOptions.put("0", "禁用");
            result.put("statusOptions", statusOptions);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取枚举值失败", e);
            return Result.error(ResultCode.ERROR, "获取枚举值失败");
        }
    }
    
    /**
     * 获取项目类型枚举
     */
    @GetMapping("/enums/project-types")
    public Result<List<Map<String, Object>>> getProjectTypes() {
        log.info("获取项目类型枚举");
        try {
            return Result.success(SalaryEnums.ProjectType.toList());
        } catch (Exception e) {
            log.error("获取项目类型失败", e);
            return Result.error(ResultCode.ERROR, "获取失败");
        }
    }
    
    /**
     * 获取项目类别枚举
     */
    @GetMapping("/enums/project-categories")
    public Result<List<Map<String, Object>>> getProjectCategories() {
        log.info("获取项目类别枚举");
        try {
            return Result.success(SalaryEnums.ProjectCategory.toList());
        } catch (Exception e) {
            log.error("获取项目类别失败", e);
            return Result.error(ResultCode.ERROR, "获取失败");
        }
    }
    
    /**
     * 获取计算方法枚举
     */
    @GetMapping("/enums/calculation-methods")
    public Result<List<Map<String, Object>>> getCalculationMethods() {
        log.info("获取计算方法枚举");
        try {
            return Result.success(SalaryEnums.CalculationMethod.toList());
        } catch (Exception e) {
            log.error("获取计算方法失败", e);
            return Result.error(ResultCode.ERROR, "获取失败");
        }
    }
    
    /**
     * 获取预定义项目编码枚举
     */
    @GetMapping("/enums/predefined-codes")
    public Result<List<Map<String, Object>>> getPredefinedProjectCodes() {
        log.info("获取预定义项目编码枚举");
        try {
            return Result.success(SalaryEnums.ProjectCode.toList());
        } catch (Exception e) {
            log.error("获取预定义编码失败", e);
            return Result.error(ResultCode.ERROR, "获取失败");
        }
    }
}
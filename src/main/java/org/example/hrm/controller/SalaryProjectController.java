package org.example.hrm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.example.hrm.common.PageResult;
import org.example.hrm.common.Result;
import org.example.hrm.common.ResultCode;
import org.example.hrm.constant.SalaryConstants;
import org.example.hrm.entity.SalaryProject;
import org.example.hrm.enums.SalaryEnums;
import org.example.hrm.repository.SalaryProjectRepository;
import org.example.hrm.service.SalaryProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/salary/projects")
@CrossOrigin
@Slf4j
public class SalaryProjectController {
    @Autowired SalaryProjectService salaryProjectService;
    @Autowired SalaryProjectRepository salaryProjectRepository;

    /* 获取所有薪酬项目 */
    @GetMapping("/list")
    public Result<List<SalaryProject>>getAllProjects(){
     log.info("获取所有薪酬项目");
     List<SalaryProject> projects = salaryProjectService.getAllProjects();
     return Result.success(projects);   
    }

    /**
     * 分页查询薪酬项目
     */
    @GetMapping("/page")
    public Result<PageResult<SalaryProject>> getProjectsByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Integer projectType) {
        
        log.info("分页查询薪酬项目，页码: {}, 页大小: {}, 项目名称: {}, 项目类型: {}", 
                pageNum, pageSize, projectName, projectType);
        
        // 创建分页请求
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("sortOrder").ascending());
        
        // 这里可以添加过滤条件，为了简化先查询所有
        Page<SalaryProject> page = salaryProjectService.getProjectsByPage(pageable);
        
        // 转换为PageResult
        PageResult<SalaryProject> pageResult = new PageResult<>();
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(page.getTotalElements());
        pageResult.setTotalPages(page.getTotalPages());
        pageResult.setList(page.getContent());
        
        return Result.success(pageResult);
    }

     /**
     * 根据ID获取薪酬项目
     */
    @GetMapping("/{id}")
    public Result<SalaryProject> getProjectById(@PathVariable Long id) {
        log.info("根据ID获取薪酬项目: {}", id);
        SalaryProject project = salaryProjectService.getProjectById(id);
        return Result.success(project);
    }

      /**
     * 创建新的薪酬项目
     */
    @PostMapping
    public Result<SalaryProject> createProject(@Valid @RequestBody SalaryProject project) {
        log.info("创建新的薪酬项目: {}", project.getProjectCode());
        
        SalaryProject createdProject = salaryProjectService.createProject(project);
        return Result.success(ResultCode.SUCCESS, createdProject);
    }
    
    /**
     * 更新薪酬项目
     */
    @PutMapping("/{id}")
    public Result<SalaryProject> updateProject(@PathVariable Long id, 
                                             @Valid @RequestBody SalaryProject project) {
        log.info("更新薪酬项目: {}", id);
        
        SalaryProject updatedProject = salaryProjectService.updateProject(id, project);
        return Result.success(ResultCode.SUCCESS, updatedProject);
    }

    /**
 * 检查项目编码是否存在
 */
@GetMapping("/check-code/{projectCode}")
public Result<Map<String, Object>> checkProjectCode(
        @PathVariable String projectCode,
        @RequestParam(required = false) Long excludeId) {
    
    log.info("检查项目编码是否存在: {}, 排除ID: {}", projectCode, excludeId);
    
    Optional<SalaryProject> project = salaryProjectRepository.findByProjectCode(projectCode);
    
    boolean exists;
    if (project.isPresent()) {
        if (excludeId != null) {
            exists = !project.get().getId().equals(excludeId);
        } else {
            exists = true;
        }
    } else {
        exists = false;
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("projectCode", projectCode);
    result.put("exists", exists);
    result.put("available", !exists);
    
    return Result.success(result);
}
    
    /**
     * 删除薪酬项目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        log.info("删除薪酬项目: {}", id);
        
        salaryProjectService.deleteProject(id);
        return Result.success();
    }
    
    /**
     * 批量删除薪酬项目
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteProjects(@RequestBody List<Long> projectIds) {
        log.info("批量删除薪酬项目，数量: {}", projectIds.size());
        
        salaryProjectService.batchDeleteProjects(projectIds);
        return Result.success();
    }

    
    
    /**
     * 获取项目类型列表
     */
    @GetMapping("/types")
    public Result<List<Map<String, Object>>> getProjectTypes() {
        log.info("获取项目类型列表");
        return Result.success(SalaryEnums.ProjectType.toList());
    }
    
    /**
     * 获取项目类别列表
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getProjectCategories() {
        log.info("获取项目类别列表");
        return Result.success(SalaryEnums.ProjectCategory.toList());
    }
    
    /**
     * 获取计算方法列表
     */
    @GetMapping("/calculation-methods")
    public Result<List<Map<String, Object>>> getCalculationMethods() {
        log.info("获取计算方法列表");
        return Result.success(SalaryEnums.CalculationMethod.toList());
    }
    
    /**
     * 获取预定义的项目编码列表
     */
    @GetMapping("/predefined-codes")
    public Result<List<Map<String, Object>>> getPredefinedProjectCodes() {
        log.info("获取预定义的项目编码列表");
        return Result.success(SalaryEnums.ProjectCode.toList());
    }
    
    /**
     * 获取薪酬项目管理相关的枚举值和常量
     */
    @GetMapping("/enums")
    public Result<Map<String, Object>> getEnumsAndConstants() {
        log.info("获取薪酬项目管理相关的枚举值和常量");
        
        Map<String, Object> result = new HashMap<>();
        result.put("projectTypes", SalaryEnums.ProjectType.toList());
        result.put("projectCategories", SalaryEnums.ProjectCategory.toList());
        result.put("calculationMethods", SalaryEnums.CalculationMethod.toList());
        result.put("predefinedProjectCodes", SalaryEnums.ProjectCode.toList());
        
        // 添加一些常量
        Map<String, Object> constants = new HashMap<>();
        constants.put("PROJECT_TYPE_INCOME", SalaryConstants.PROJECT_TYPE_INCOME);
        constants.put("PROJECT_TYPE_DEDUCTION", SalaryConstants.PROJECT_TYPE_DEDUCTION);
        constants.put("DEFAULT_WORK_DAYS_PER_MONTH", SalaryConstants.DEFAULT_WORK_DAYS_PER_MONTH);
        
        result.put("constants", constants);
        
        return Result.success(result);
    }



}

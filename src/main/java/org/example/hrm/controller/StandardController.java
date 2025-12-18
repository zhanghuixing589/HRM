// StandardController.java
package org.example.hrm.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.hrm.common.BusinessException;
import org.example.hrm.common.PageResult;
import org.example.hrm.common.Result;
import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.StandardDTO;
import org.example.hrm.dto.StandardDetailResponseDTO;
import org.example.hrm.dto.StandardResponseDTO;
import org.example.hrm.entity.Standard;
import org.example.hrm.entity.StandardApproval;
import org.example.hrm.entity.StandardApprovalLog;
import org.example.hrm.enums.SalaryEnums;
import org.example.hrm.service.StandardService;
import org.example.hrm.vo.PositionVO;
import org.example.hrm.vo.StandardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/salary/standards")
@CrossOrigin(origins = "*")
@Slf4j
@Validated
public class StandardController {

    @Autowired
    private StandardService standardService;

    /**
     * 生成标准编码
     */
    @GetMapping("/generate-code")
   // @PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST','FINANCE_MANAGER')")
    public Result<Map<String, String>> generateStandardCode() {
        log.info("生成标准编码");
        
        try {
            String code = standardService.generateStandardCode();
            Map<String, String> result = new HashMap<>();
            result.put("standardCode", code);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成标准编码失败", e);
            return Result.error("生成标准编码失败：" + e.getMessage());
        }
    }

    /**
     * 初始化薪酬标准表单所需的所有数据
     */
    @GetMapping("/init-form")
    //@PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST','FINANCE_MANAGER')")
    public Result<Map<String, Object>> initFormData() {
        log.info("初始化薪酬标准表单数据");
        
        try {
            Map<String, Object> formData = standardService.initFormData();
            return Result.success(formData);
        } catch (BusinessException e) {
            log.error("初始化表单数据业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("初始化表单数据失败", e);
            return Result.error("初始化表单数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有职位数据（用于选择）
     */
    @GetMapping("/positions/all")
 // @PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST', 'FINANCE_MANAGER')")
    public Result<List<Map<String, Object>>> getAllPositions() {
        log.info("获取所有职位数据");
        
        try {
            List<Map<String, Object>> positions = standardService.getPositionsForSelection();
            for(Map<String, Object> position : positions){
                System.out.println(position);
            }
            return Result.success(positions);
        } catch (BusinessException e) {
            log.error("获取职位数据业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取职位数据失败", e);
            return Result.error("获取职位数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有薪酬项目数据
     */
    @GetMapping("/projects/all")
   // @PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST', 'FINANCE_MANAGER')")
    public Result<List<Map<String, Object>>> getAllProjects() {
        log.info("获取所有薪酬项目数据");
        
        try {
            List<Map<String, Object>> projects = standardService.getProjectsForSelection();
            return Result.success(projects);
        } catch (BusinessException e) {
            log.error("获取薪酬项目数据业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取薪酬项目数据失败", e);
            return Result.error("获取薪酬项目数据失败：" + e.getMessage());
        }
    }

    /**
     * 创建薪酬标准
     */
    @PostMapping("/create")
   // @PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST')")
    public Result<StandardVO> createStandard(
            @Valid @RequestBody StandardDTO dto) {
        
       // 从 DTO 中获取创建人ID
    Long creatorId = dto.getCreatorId();

     // 如果还是没有 creatorId，返回错误
    if (creatorId == null) {
        log.error("未获取到创建人ID");
        return Result.error("未获取到用户信息，请重新登录");
    }
    
        
        try {
            StandardVO standard = standardService.createStandard(dto, creatorId);
            return Result.success("创建成功", standard);
        } catch (BusinessException e) {
            log.error("创建薪酬标准业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建薪酬标准失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新薪酬标准
     */
    @PutMapping("/{id}")
  //  @PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST')")
    public Result<StandardVO> updateStandard(
            @PathVariable Long id,
            @Valid @RequestBody StandardDTO dto,
            @RequestAttribute Long userId) {
        
        log.info("更新薪酬标准，ID：{}，更新人：{}", id, userId);

        System.out.println("接受："+dto.getStandardName());
        
        try {
            StandardVO standard = standardService.updateStandard(id, dto, userId);
            System.out.println("===============标准，ID："+ id);
            System.out.println("===============标准，ID："+ userId);
            return Result.success("更新成功", standard);
        } catch (BusinessException e) {
            log.error("更新薪酬标准业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新薪酬标准失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
 * 检查职位是否被占用
 */
@GetMapping("/positions/{positionId}/check-occupied")
public Result<Map<String, Object>> checkPositionIsOccupied(
        @PathVariable Long positionId,
        @RequestParam(required = false) Long excludeStandardId) {
    log.info("检查职位占用状态，职位ID：{}，排除标准ID：{}", positionId, excludeStandardId);
    
    try {
        boolean isOccupied = standardService.checkPositionIsOccupied(positionId, excludeStandardId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("positionId", positionId);
        result.put("isOccupied", isOccupied);
        result.put("excludeStandardId", excludeStandardId);
        
        if (isOccupied) {
            result.put("message", "该职位已被其他薪酬标准占用");
        } else {
            result.put("message", "该职位可用");
        }
        
        return Result.success(result);
    } catch (Exception e) {
        log.error("检查职位占用状态失败", e);
        return Result.error("检查失败：" + e.getMessage());
    }
}



/**
 * 批量检查职位占用状态
 */
@PostMapping("/positions/batch-check-occupied")
public Result<Map<String, Object>> batchCheckPositionsOccupied(
        @RequestBody List<Long> positionIds,
        @RequestParam(required = false) Long excludeStandardId) {
    log.info("批量检查职位占用状态，职位数量：{}，排除标准ID：{}", positionIds.size(), excludeStandardId);
    
    try {
        Map<Long, Boolean> occupiedMap = new HashMap<>();
        List<Map<String, Object>> occupiedDetails = new ArrayList<>();
        
        for (Long positionId : positionIds) {
            boolean isOccupied = standardService.checkPositionIsOccupied(positionId, excludeStandardId);
            occupiedMap.put(positionId, isOccupied);
            
            if (isOccupied) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("positionId", positionId);
                detail.put("isOccupied", true);
                detail.put("message", "已被占用");
                occupiedDetails.add(detail);
            }
        }
        
        long occupiedCount = occupiedMap.values().stream().filter(v -> v).count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", positionIds.size());
        result.put("occupiedCount", occupiedCount);
        result.put("availableCount", positionIds.size() - occupiedCount);
        result.put("occupiedMap", occupiedMap);
        result.put("occupiedDetails", occupiedDetails);
        result.put("excludeStandardId", excludeStandardId);
        
        return Result.success(result);
    } catch (Exception e) {
        log.error("批量检查职位占用状态失败", e);
        return Result.error("批量检查失败：" + e.getMessage());
    }
}



    /**
 * 获取标准详情（包含所有关联数据）
 */
@GetMapping("/{id}/detail")
public Result<StandardDetailResponseDTO> getStandardDetail(@PathVariable Long id) {
    log.info("获取标准详情（含关联数据），ID：{}", id);
    
    try {
        StandardDetailResponseDTO detail = standardService.getStandardDetailById(id);
        return Result.success(detail);
    } catch (BusinessException e) {
        log.error("获取标准详情业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("获取标准详情失败", e);
        return Result.error("获取详情失败：" + e.getMessage());
    }
}

    /**
     * 提交审核
     */
    @PostMapping("/{id}/submit")
    //@PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST')")
    public Result<Void> submitForApproval(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestAttribute Long userId) {
        
        log.info("提交审核，标准ID：{}，提交人：{}", id, userId);
        
        try {
            standardService.submitForApproval(id, userId, comment);
            return Result.success();
        } catch (BusinessException e) {
            log.error("提交审核业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("提交审核失败", e);
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    public Result<Void> approveStandard(
            @PathVariable Long id,
            @RequestParam(required = false) String opinion,
            @RequestAttribute Long userId) {
        
        log.info("审核通过，标准ID：{}，审核人：{}", id, userId);
        
        try {
            standardService.approveStandard(id, userId, opinion);
            return Result.success();
        } catch (BusinessException e) {
            log.error("审核通过业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("审核通过失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 审核驳回
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    public Result<Void> rejectStandard(
            @PathVariable Long id,
            @RequestParam(required = false) String opinion,
            @RequestAttribute Long userId) {
        
        log.info("审核驳回，标准ID：{}，审核人：{}", id, userId);
        
        try {
            standardService.rejectStandard(id, userId, opinion);
            return Result.success();
        } catch (BusinessException e) {
            log.error("审核驳回业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("审核驳回失败", e);
            return Result.error("驳回失败：" + e.getMessage());
        }
    }

    /**
     * 获取标准详情
     */
    @GetMapping("/{id}")
    public Result<StandardVO> getStandardById(@PathVariable Long id) {
        log.info("获取标准详情，ID：{}", id);
        
        try {
            StandardVO standard = standardService.getStandardById(id);
            return Result.success(standard);
        } catch (BusinessException e) {
            log.error("获取标准详情业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取标准详情失败", e);
            return Result.error("获取详情失败：" + e.getMessage());
        }
    }

    /**
     * 根据编码获取标准详情
     */
    @GetMapping("/code/{standardCode}")
    public Result<StandardVO> getStandardByCode(@PathVariable String standardCode) {
        log.info("获取标准详情，编码：{}", standardCode);
        
        try {
            StandardVO standard = standardService.getStandardByCode(standardCode);
            return Result.success(standard);
        } catch (BusinessException e) {
            log.error("获取标准详情业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取标准详情失败", e);
            return Result.error("获取详情失败：" + e.getMessage());
        }
    }
    /* 获取所有标准主标 */
    @GetMapping("/list")
    public Result<List<StandardResponseDTO>> getAllStandards() {
        log.info("获取所有标准主标");

        try {
            List<Standard> standards = standardService.getAllStandards();
            List<StandardResponseDTO> dtos = standardService.convertToResponseDTOList(standards);
            return Result.success(dtos);
        } catch (Exception e) {
            log.error("获取所有标准主标失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

   /**
 * 分页查询薪酬标准
 */
@GetMapping("/page")
public Result<PageResult<StandardResponseDTO>> getStandardsByPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String standardCode,
        @RequestParam(required = false) String standardName,
        @RequestParam(required = false) Integer status) {
            log.info("分页查询薪酬标准，pageNum：{}，pageSize：{}，standardCode：{}，standardName：{}，status：{}",
                    pageNum, pageSize, standardCode, standardName, status);

                    try{
                        //创建分页请求
                        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

                        //调用服务方法查询薪酬标准
                        Page<Standard> page = standardService.getStandardByPage(pageable);

                        //将查询结果转换为响应DTO
                        List<StandardResponseDTO> dtos = standardService.convertToResponseDTOList(page.getContent());

                        //创建分页结果
                        PageResult <StandardResponseDTO> pageResult =  new PageResult<>();
                        pageResult.setPageNum(pageNum);
                        pageResult.setPageSize(pageSize);
                        pageResult.setTotal(page.getTotalElements());
                        pageResult.setTotalPages(page.getTotalPages());
                        pageResult.setList(dtos);
                        return Result.success(pageResult);
                    }catch (Exception e){

                        log.error("分页查询薪酬标准失败", e);
                        return Result.error("查询失败：" + e.getMessage());
                    }

        }

    /**
     * 删除薪酬标准
     */
    @DeleteMapping("/{id}")
  //  @PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST','FINANCE_MANAGER')")
    public Result<Void> deleteStandard(@PathVariable Long id) {
        log.info("删除薪酬标准，ID：{}", id);
        
        try {
            standardService.deleteStandard(id);
            return Result.success();
        } catch (BusinessException e) {
            log.error("删除薪酬标准业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除薪酬标准失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除薪酬标准
     */
    @DeleteMapping("/batch")
    //@PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST','FINANCE_MANAGER')")
    public Result<Void> batchDeleteStandards(@RequestBody List<Long> ids) {
        log.info("批量删除薪酬标准，数量：{}", ids.size());
        
        try {
            standardService.batchDeleteStandards(ids);
            return Result.success();
        } catch (BusinessException e) {
            log.error("批量删除薪酬标准业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量删除薪酬标准失败", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取审核记录
     */
/*     @GetMapping("/{id}/approval-history")
    public Result<List<StandardApproval>> getApprovalHistory(@PathVariable Long id) {
        log.info("获取审核记录，标准ID：{}", id);
        
        try {
            List<StandardApproval> history = standardService.getApprovalHistory(id);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取审核记录失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    } */

    /**
     * 获取审核日志
     */
  /*   @GetMapping("/approval/{approvalId}/logs")
    public Result<List<StandardApprovalLog>> getApprovalLogs(@PathVariable Long approvalId) {
        log.info("获取审核日志，审核ID：{}", approvalId);
        
        try {
            List<StandardApprovalLog> logs = standardService.getApprovalLogs(approvalId);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("获取审核日志失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    } */

    /**
 * 获取标准审核详情（专为审核页面设计）
 */
@GetMapping("/{id}/approval-detail")
@PreAuthorize("hasAnyRole('FINANCE_MANAGER', 'HR_MANAGER')")
public Result<StandardDetailResponseDTO> getStandardApprovalDetail(@PathVariable Long id) {
    log.info("获取标准审核详情，ID：{}", id);
    
    try {
        StandardDetailResponseDTO detail = standardService.getStandardApprovalDetail(id);
        return Result.success(detail);
    } catch (BusinessException e) {
        log.error("获取审核详情业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("获取审核详情失败", e);
        return Result.error("获取审核详情失败：" + e.getMessage());
    }
}

/**
 * 获取标准审核历史记录（包含详细日志）
 */
@GetMapping("/{id}/approval-history-detailed")
@PreAuthorize("hasAnyRole('FINANCE_MANAGER', 'HR_MANAGER', 'FINANCE_SPECIALIST')")
public Result<List<StandardDetailResponseDTO.ApprovalHistoryDTO>> getDetailedApprovalHistory(
        @PathVariable Long id) {
    log.info("获取详细审核历史记录，标准ID：{}", id);
    
    try {
        List<StandardDetailResponseDTO.ApprovalHistoryDTO> history = 
            standardService.getApprovalHistory(id);
        return Result.success(history);
    } catch (BusinessException e) {
        log.error("获取详细审核历史业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("获取详细审核历史失败", e);
        return Result.error("获取详细审核历史失败：" + e.getMessage());
    }
}

/**
 * 获取详细的审核日志
 */
@GetMapping("/{id}/approval-logs-detailed")
@PreAuthorize("hasAnyRole('FINANCE_MANAGER', 'HR_MANAGER', 'FINANCE_SPECIALIST')")
public Result<List<StandardDetailResponseDTO.ApprovalLogDTO>> getDetailedApprovalLogs(
        @PathVariable Long id) {
    log.info("获取详细审核日志，标准ID：{}", id);
    
    try {
        List<StandardDetailResponseDTO.ApprovalLogDTO> logs = 
            standardService.getDetailedApprovalLogs(id);
        return Result.success(logs);
    } catch (BusinessException e) {
        log.error("获取详细审核日志业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("获取详细审核日志失败", e);
        return Result.error("获取详细审核日志失败：" + e.getMessage());
    }
}

    /**
     * 获取所有职位（用于选择）
     */
    @GetMapping("/positions")
    //@PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST', 'FINANCE_MANAGER')")
    public Result<List<Map<String, Object>>> getAllPositionsForSelection() {
        log.info("获取所有职位（用于选择）");
        
        try {
            List<Map<String, Object>> positions = standardService.getPositionsForSelection();
            return Result.success(positions);
        } catch (Exception e) {
            log.error("获取职位列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取指定机构的职位
     */
    @GetMapping("/positions/org/{orgId}")
    //@PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST','FINANCE_MANAGER')")
    public Result<List<PositionVO>> getPositionsByOrgId(@PathVariable Long orgId) {
        log.info("获取指定机构的职位，机构ID：{}", orgId);
        
        try {
            List<PositionVO> positions = standardService.getPositionsByOrgId(orgId);
            return Result.success(positions);
        } catch (Exception e) {
            log.error("获取职位列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

/**
 * 检查职位是否已有关联标准
 */
/**
 * 检查职位是否已有关联标准（支持排除当前标准）
 */
@GetMapping("/positions/{positionId}/has-standard")
public Result<Map<String, Object>> checkPositionHasStandard(
        @PathVariable Long positionId,
        @RequestParam(required = false) Long currentStandardId) {
    log.info("检查职位是否已有关联标准，职位ID：{}，当前标准ID：{}", positionId, currentStandardId);
    
    try {
        boolean hasStandard = standardService.checkPositionHasStandard(positionId, currentStandardId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("positionId", positionId);
        result.put("hasStandard", hasStandard);
        result.put("currentStandardId", currentStandardId);
        
        return Result.success(result);
    } catch (Exception e) {
        log.error("检查职位关联失败", e);
        return Result.error("检查失败：" + e.getMessage());
    }
}

    /**
     * 获取薪酬项目用于选择
     */
    @GetMapping("/projects")
  // @PreAuthorize("hasAnyRole('HR_MANAGER', 'FINANCE_SPECIALIST','FINANCE_MANAGER')")
    public Result<List<Map<String, Object>>> getProjectsForSelection() {
        log.info("获取薪酬项目用于选择");
        
        try {
            List<Map<String, Object>> projects = standardService.getProjectsForSelection();
            return Result.success(projects);
        } catch (Exception e) {
            log.error("获取薪酬项目失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取枚举值
     */
    @GetMapping("/enums")
    public Result<Map<String, Object>> getEnums() {
        log.info("获取薪酬标准相关的枚举值");
        
        try {
            Map<String, Object> result = standardService.getAllEnums();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取枚举值失败", e);
            return Result.error("获取枚举值失败");
        }
    }

    /**
     * 获取标准状态枚举
     */
    @GetMapping("/enums/standard-status")
    public Result<List<Map<String, Object>>> getStandardStatuses() {
        log.info("获取标准状态枚举");
        try {
            Map<String, Object> enums = standardService.getAllEnums();
            return Result.success((List<Map<String, Object>>) enums.get("standardStatuses"));
        } catch (Exception e) {
            log.error("获取标准状态失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取项目类型枚举
     */
    @GetMapping("/enums/project-types")
    public Result<List<Map<String, Object>>> getProjectTypes() {
        log.info("获取项目类型枚举");
        try {
            Map<String, Object> enums = standardService.getAllEnums();
            return Result.success((List<Map<String, Object>>) enums.get("projectTypes"));
        } catch (Exception e) {
            log.error("获取项目类型失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取项目类别枚举
     */
    @GetMapping("/enums/project-categories")
    public Result<List<Map<String, Object>>> getProjectCategories() {
        log.info("获取项目类别枚举");
        try {
            Map<String, Object> enums = standardService.getAllEnums();
            return Result.success((List<Map<String, Object>>) enums.get("projectCategories"));
        } catch (Exception e) {
            log.error("获取项目类别失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取计算方法枚举
     */
    @GetMapping("/enums/calculation-methods")
    public Result<List<Map<String, Object>>> getCalculationMethods() {
        log.info("获取计算方法枚举");
        try {
            Map<String, Object> enums = standardService.getAllEnums();
            return Result.success((List<Map<String, Object>>) enums.get("calculationMethods"));
        } catch (Exception e) {
            log.error("获取计算方法失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}
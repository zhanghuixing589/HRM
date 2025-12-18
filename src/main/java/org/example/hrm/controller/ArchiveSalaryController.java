// ArchiveSalaryController.java - 新Controller
package org.example.hrm.controller;

import org.example.hrm.common.Result;
import org.example.hrm.service.ArchiveSalaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/archive/salary")
@CrossOrigin
@Slf4j
public class ArchiveSalaryController {
    
    @Autowired
    private ArchiveSalaryService archiveSalaryService;
    
    /**
     * 根据职位和职称获取薪酬标准
     */
    @GetMapping("/by-position-title")
    public Result<List<Map<String, Object>>> getStandardsByPositionAndTitle(
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) String title) {
        
        try {
            List<Map<String, Object>> standards = 
                archiveSalaryService.getStandardsForPosition(positionId);
            
            // 如果提供了职称，在前端进行过滤
            if (title != null && !title.isEmpty()) {
                String titleLower = title.toLowerCase();
                standards = standards.stream()
                    .filter(standard -> {
                        String name = (String) standard.get("standardName");
                        return name != null && name.toLowerCase().contains(titleLower);
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return Result.success(standards);
        } catch (Exception e) {
            log.error("获取薪酬标准失败", e);
            return Result.error("获取薪酬标准失败");
        }
    }
    
    /**
     * 搜索薪酬标准
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchStandards(
            @RequestParam(required = false) String keyword) {
        
        try {
            List<Map<String, Object>> standards = archiveSalaryService.searchStandards(keyword)
                .stream()
                .map(standard -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("standardId", standard.getStandardId());
                    map.put("standardCode", standard.getStandardCode());
                    map.put("standardName", standard.getStandardName());
                    map.put("status", standard.getStatus());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return Result.success(standards);
        } catch (Exception e) {
            log.error("搜索薪酬标准失败", e);
            return Result.error("搜索薪酬标准失败");
        }
    }
    
    /**
     * 获取所有已生效的薪酬标准
     */
    @GetMapping("/all-active")
    public Result<List<Map<String, Object>>> getAllActiveStandards() {
        try {
            List<Map<String, Object>> standards = archiveSalaryService.getAllActiveStandards()
                .stream()
                .map(standard -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("standardId", standard.getStandardId());
                    map.put("standardCode", standard.getStandardCode());
                    map.put("standardName", standard.getStandardName());
                    map.put("status", standard.getStatus());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return Result.success(standards);
        } catch (Exception e) {
            log.error("获取所有薪酬标准失败", e);
            return Result.error("获取所有薪酬标准失败");
        }
    }
}
package org.example.hrm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hrm.common.Result;
import org.example.hrm.service.SalaryCalculator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/salary/calculation")
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
public class SalaryCalculationController {
    
    private final SalaryCalculator salaryCalculator;
    
    /**
     * 计算员工薪酬
     */
    @PostMapping("/calculate")
    public Result<?> calculateEmployeeSalary(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") LocalDate salaryMonth) {
        
        log.info("计算员工薪酬 - 用户ID: {}, 月份: {}", userId, salaryMonth);
        
        try {
            Object result = salaryCalculator.calculateEmployeeSalary(userId, salaryMonth);
            return Result.success("薪酬计算成功", result);
        } catch (Exception e) {
            log.error("薪酬计算失败", e);
            return Result.error("薪酬计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取计算策略列表
     */
    @GetMapping("/strategies")
    public Result<Map<String, String>> getCalculationStrategies() {
        // 这里应该从策略工厂获取策略列表
        Map<String, String> strategies = new HashMap<>();
        strategies.put("固定值", "FixedAmountStrategy");
        strategies.put("百分比", "PercentageStrategy");
        strategies.put("公式计算", "FormulaStrategy");
        strategies.put("日工资", "DailySalaryStrategy");
        strategies.put("阶梯计算", "StepStrategy");
        
        return Result.success(strategies);
    }
}
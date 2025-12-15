package org.example.hrm.context;

import lombok.Data;
import org.example.hrm.entity.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 薪酬计算上下文
 * 包含计算所需的所有信息
 */
@Data
public class SalaryCalculationContext {
    // 员工信息
    private User employee;
    
    // 薪酬计算月份
    private LocalDate salaryMonth;
    
    // 工资信息
    private BigDecimal standardBaseSalary;
    private BigDecimal actualBaseSalary;
    
    // 工作天数
    private Integer actualWorkDays;
    private Integer totalWorkDays;
    
    // 状态标志
    private boolean inProbation = false;
    
    // 计算参数存储
    private Map<String, Object> calculationParams = new HashMap<>();
    
    // 中间计算结果
    private Map<String, BigDecimal> intermediateResults = new HashMap<>();
    
    /**
     * 创建薪酬计算上下文
     */
    public static SalaryCalculationContext create(User employee, LocalDate salaryMonth, 
                                                BigDecimal standardBaseSalary) {
        SalaryCalculationContext context = new SalaryCalculationContext();
        context.setEmployee(employee);
        context.setSalaryMonth(salaryMonth);
        context.setStandardBaseSalary(standardBaseSalary);
        
        // 初始化
        initializeContext(context);
        
        return context;
    }
    
    private static void initializeContext(SalaryCalculationContext context) {
        // 判断试用期
        if (context.getEmployee() != null && context.getSalaryMonth() != null) {
            context.setInProbation(context.getEmployee().isInProbation(context.getSalaryMonth()));
        }
        
        // 计算实际基本工资
        calculateActualBaseSalary(context);
        
        // 设置默认工作天数
        context.setTotalWorkDays(22);
        
        // 计算实际工作天数
        if (context.getEmployee() != null) {
            int workDays = context.getEmployee().calculateWorkDaysInMonth(context.getSalaryMonth());
            context.setActualWorkDays(workDays);
        }
    }
    
    private static void calculateActualBaseSalary(SalaryCalculationContext context) {
        if (context.isInProbation()) {
            // 试用期工资为标准的50%
            context.setActualBaseSalary(
                context.getStandardBaseSalary()
                    .multiply(new BigDecimal("0.5"))
                    .setScale(2, RoundingMode.HALF_UP)
            );
        } else {
            context.setActualBaseSalary(context.getStandardBaseSalary());
        }
    }
    
    /**
     * 添加计算参数
     */
    public void addCalculationParam(String key, Object value) {
        calculationParams.put(key, value);
    }
    
    /**
     * 获取计算参数
     */
    public Object getCalculationParam(String key) {
        return calculationParams.get(key);
    }
    
    /**
     * 存储中间结果
     */
    public void storeIntermediateResult(String key, BigDecimal value) {
        intermediateResults.put(key, value);
    }
    
    /**
     * 获取中间结果
     */
    public BigDecimal getIntermediateResult(String key) {
        return intermediateResults.get(key);
    }
    
    /**
     * 获取日工资
     */
    public BigDecimal getDailySalary() {
        if (totalWorkDays == null || totalWorkDays == 0) {
            return BigDecimal.ZERO;
        }
        return actualBaseSalary.divide(
            new BigDecimal(totalWorkDays), 4, RoundingMode.HALF_UP
        ).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 判断是否为离职员工
     */
    public boolean isResignedEmployee() {
        return employee != null && employee.isResigned();
    }
    
    /**
     * 获取实际工作天数比例
     */
    public BigDecimal getWorkDayRatio() {
        if (totalWorkDays == null || totalWorkDays == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(actualWorkDays)
                .divide(new BigDecimal(totalWorkDays), 4, RoundingMode.HALF_UP);
    }
}
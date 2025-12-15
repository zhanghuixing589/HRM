package org.example.hrm.strategy;

import org.example.hrm.context.SalaryCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 百分比计算策略
 */
@Component
public class PercentageStrategy implements SalaryCalculationStrategy {
    
    @Override
    public BigDecimal calculate(SalaryCalculationContext context, Map<String, Object> params) {
        // 获取百分比参数
        BigDecimal percentage = (BigDecimal) params.get("percentage");
        if (percentage == null) {
            // 尝试从字符串转换
            Object value = params.get("percentage");
            if (value instanceof String) {
                try {
                    percentage = new BigDecimal((String) value);
                } catch (Exception e) {
                    return BigDecimal.ZERO;
                }
            } else if (value instanceof Number) {
                percentage = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                return BigDecimal.ZERO;
            }
        }
        
        // 获取基数类型
        String baseType = (String) params.get("baseType");
        BigDecimal baseAmount = getBaseAmount(context, baseType);
        
        // 将百分比转换为小数（例如20%转换为0.2）
        BigDecimal ratio = percentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        // 计算金额
        BigDecimal amount = baseAmount.multiply(ratio)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 如果是离职或试用期，按实际工作天数比例计算
        if (context.getEmployee() != null && 
            (context.getEmployee().isResigned() || context.isInProbation())) {
            return calculateProratedAmount(amount, context);
        }
        
        return amount;
    }
    
    private BigDecimal getBaseAmount(SalaryCalculationContext context, String baseType) {
        if ("standardBaseSalary".equals(baseType)) {
            return context.getStandardBaseSalary();
        } else if ("actualBaseSalary".equals(baseType)) {
            return context.getActualBaseSalary();
        } else {
            // 默认使用实际基本工资
            return context.getActualBaseSalary();
        }
    }
    
    private BigDecimal calculateProratedAmount(BigDecimal amount, SalaryCalculationContext context) {
        int totalDays = context.getTotalWorkDays();
        int actualDays = context.getActualWorkDays();
        
        if (totalDays <= 0 || actualDays <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal ratio = new BigDecimal(actualDays)
                .divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP);
        return amount.multiply(ratio)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getName() {
        return "百分比";
    }
    
    @Override
    public String getDescription() {
        return "基于基本工资按百分比计算";
    }
}
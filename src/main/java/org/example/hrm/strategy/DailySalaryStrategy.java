package org.example.hrm.strategy;

import org.example.hrm.context.SalaryCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 日工资计算策略
 */
@Component
public class DailySalaryStrategy implements SalaryCalculationStrategy {
    
    @Override
    public BigDecimal calculate(SalaryCalculationContext context, Map<String, Object> params) {
        // 获取每日标准工资
        BigDecimal dailyRate = (BigDecimal) params.get("dailyRate");
        if (dailyRate == null) {
            // 如果没有指定每日标准，则根据基本工资计算
            dailyRate = context.getActualBaseSalary()
                    .divide(new BigDecimal(context.getTotalWorkDays()), 4, RoundingMode.HALF_UP);
        }
        
        // 实际工作天数
        int workDays = context.getActualWorkDays();
        
        return dailyRate.multiply(new BigDecimal(workDays))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getName() {
        return "日工资";
    }
    
    @Override
    public String getDescription() {
        return "按日工资标准乘以实际工作天数计算";
    }
}
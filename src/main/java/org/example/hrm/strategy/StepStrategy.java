package org.example.hrm.strategy;

import org.example.hrm.context.SalaryCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 阶梯计算策略（用于绩效奖金等）
 */
@Component
public class StepStrategy implements SalaryCalculationStrategy {
    
    @Override
    public BigDecimal calculate(SalaryCalculationContext context, Map<String, Object> params) {
        // 获取绩效等级
        String performanceLevel = (String) params.get("performanceLevel");
        Map<String, BigDecimal> stepRates = (Map<String, BigDecimal>) params.get("stepRates");
        
        if (performanceLevel == null || stepRates == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal rate = stepRates.get(performanceLevel);
        if (rate == null) {
            return BigDecimal.ZERO;
        }
        
        return context.getActualBaseSalary()
                .multiply(rate)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
    
    @Override
    public String getName() {
        return "阶梯计算";
    }
    
    @Override
    public String getDescription() {
        return "根据不同的等级或阶梯标准计算金额";
    }
}
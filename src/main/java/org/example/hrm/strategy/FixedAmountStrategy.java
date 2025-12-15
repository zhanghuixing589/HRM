package org.example.hrm.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.example.hrm.context.SalaryCalculationContext;
import org.springframework.stereotype.Component;

/**
 * 固定值计算策略
 */
@Component
public class FixedAmountStrategy implements SalaryCalculationStrategy {
    @Override
    public BigDecimal calculate(SalaryCalculationContext context,Map<String,Object> params){
        //获取固定金额参数
        BigDecimal fixedAmount = (BigDecimal) params.get("fixedAmount");
        if (fixedAmount == null) {
            //尝试从字符串转换
             Object value = params.get("fixedAmount");
              if (value instanceof String) {
                try {
                    fixedAmount = new BigDecimal((String) value);
                } catch (Exception e) {
                    return BigDecimal.ZERO;
                }
            } else if (value instanceof Number) {
                fixedAmount = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                return BigDecimal.ZERO;
            }
        }

         // 如果是离职或试用期，按实际工作天数比例计算
        if (context.getEmployee() != null && 
            (context.getEmployee().isResigned() || context.isInProbation())) {
            return calculateProratedAmount(fixedAmount, context);
        }
        
        return fixedAmount.setScale(2, RoundingMode.HALF_UP);
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
        return "固定值";
    }
    
    @Override
    public String getDescription() {
        return "直接使用固定的金额进行计算";
    }
}

package org.example.hrm.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.example.hrm.context.SalaryCalculationContext;

/**
 * 薪酬项目计算策略接口
 * 支持不同计算方式的可扩展设计
 */
public interface SalaryCalculationStrategy {
    
    /**
     * 计算项目金额
     * @param context 计算上下文
     * @param params 计算参数
     * @return 计算结果
     */
    BigDecimal calculate(SalaryCalculationContext context, Map<String, Object> params);
    
    /**
     * 获取策略名称
     */
    String getName();
    
    /**
     * 是否需要试用期特殊处理
     */
    default boolean requireProbationHandling() {
        return true;
    }
    
    /**
     * 获取策略描述
     */
    default String getDescription() {
        return "";
    }
}
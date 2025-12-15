package org.example.hrm.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪酬计算策略工厂
 * 根据计算方法动态选择策略
 */
@Component
public class SalaryCalculationStrategyFactory {
    
    private final Map<String, SalaryCalculationStrategy> strategyMap = new HashMap<>();
    
    /**
     * 构造函数，自动注入所有策略
     */
    @Autowired
    public SalaryCalculationStrategyFactory(List<SalaryCalculationStrategy> strategies) {
        for (SalaryCalculationStrategy strategy : strategies) {
            strategyMap.put(strategy.getName(), strategy);
        }
    }
    
    /**
     * 根据方法名获取策略
     */
    public SalaryCalculationStrategy getStrategy(String methodName) {
        return strategyMap.get(methodName);
    }
    
    /**
     * 获取所有可用策略
     */
    public Map<String, String> getAvailableStrategies() {
        Map<String, String> strategies = new HashMap<>();
        strategyMap.forEach((name, strategy) -> {
            strategies.put(name, strategy.getClass().getSimpleName());
        });
        return strategies;
    }
}
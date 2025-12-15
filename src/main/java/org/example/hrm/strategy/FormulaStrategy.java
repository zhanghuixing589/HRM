package org.example.hrm.strategy;

import org.example.hrm.context.SalaryCalculationContext;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 公式计算策略
 */
@Component
public class FormulaStrategy implements SalaryCalculationStrategy {
    
    private final ScriptEngine scriptEngine;
    
    public FormulaStrategy() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.scriptEngine = manager.getEngineByName("JavaScript");
    }
    
    @Override
    public BigDecimal calculate(SalaryCalculationContext context, Map<String, Object> params) {
        String formula = (String) params.get("formula");
        if (formula == null || formula.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // 准备变量
            Map<String, Object> variables = prepareVariables(context, params);
            
            // 解析公式
            String expression = buildExpression(formula, variables);
            
            // 计算表达式
            Object result = scriptEngine.eval(expression);
            
            // 转换结果
            BigDecimal amount = convertToBigDecimal(result);
            
            // 四舍五入保留两位小数
            return amount.setScale(2, RoundingMode.HALF_UP);
            
        } catch (ScriptException | NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    private Map<String, Object> prepareVariables(SalaryCalculationContext context, Map<String, Object> params) {
        Map<String, Object> variables = new HashMap<>();
        
        // 添加基本变量
        variables.put("baseSalary", context.getActualBaseSalary());
        variables.put("standardBaseSalary", context.getStandardBaseSalary());
        variables.put("workDays", context.getActualWorkDays());
        variables.put("totalDays", context.getTotalWorkDays());
        variables.put("isProbation", context.isInProbation());
        
        // 添加自定义参数
        if (params != null) {
            params.forEach((key, value) -> {
                if (!"formula".equals(key)) {
                    variables.put(key, value);
                }
            });
        }
        
        return variables;
    }
    
    private String buildExpression(String formula, Map<String, Object> variables) {
        String expression = formula;
        
        // 替换变量占位符
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (expression.contains(placeholder)) {
                Object value = entry.getValue();
                if (value instanceof BigDecimal) {
                    expression = expression.replace(placeholder, ((BigDecimal) value).toString());
                } else if (value instanceof Number) {
                    expression = expression.replace(placeholder, value.toString());
                } else if (value instanceof Boolean) {
                    expression = expression.replace(placeholder, Boolean.TRUE.equals(value) ? "1" : "0");
                }
            }
        }
        
        return expression;
    }
    
    private BigDecimal convertToBigDecimal(Object result) {
        if (result instanceof Number) {
            return BigDecimal.valueOf(((Number) result).doubleValue());
        } else if (result instanceof String) {
            try {
                return new BigDecimal((String) result);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String getName() {
        return "公式计算";
    }
    
    @Override
    public String getDescription() {
        return "使用JavaScript公式表达式进行计算，支持复杂逻辑";
    }
    
    @Override
    public boolean requireProbationHandling() {
        return false; // 公式计算通常包含试用期逻辑
    }
}
package org.example.hrm.config;

import lombok.Data;

/**
 * 薪酬项目配置类
 */
@Data
public class SalaryProjectConfig {
     private String projectCode;
    private String projectName;
    private Integer projectType;
    private String category;
    private String calculationMethod;
    
    // 计算参数
    private Double fixedAmount;      // 固定金额
    private Double percentage;       // 百分比
    private String formula;          // 计算公式
    private String baseType;         // 计算基数类型
    private Boolean probationEnabled; // 试用期是否计算
    private Boolean resignationEnabled; // 离职是否计算

}

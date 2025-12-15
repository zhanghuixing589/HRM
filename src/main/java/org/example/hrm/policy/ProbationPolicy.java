package org.example.hrm.policy;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

//试用期薪酬政策类
@Data
public class ProbationPolicy {
    //试用期月数
    private Integer probationMonths =3;
    //试用期薪酬比例
    private BigDecimal baseSalaryRatio = new BigDecimal("0.5");
    //是否计算津贴
    private Boolean calculateAllowance = true;
    //是否计算奖金
    private Boolean calculateBonus = true;
    //是否计算社保
    private Boolean calculateInsurance = false;

    //判断员工是否在试用期
    public boolean isInProbation(LocalDate entryDate,LocalDate salaryMonth){
        if (entryDate == null || salaryMonth == null) {
            return false;
            
        }

        LocalDate probationEnd = entryDate.plusMonths(probationMonths);
        //试用期结束日期
        LocalDate probationEndMonth = probationEnd.minusDays(1);

        return !salaryMonth.isAfter(probationEndMonth);
    }

    //计算试用期基本工资
     public BigDecimal calculateProbationBaseSalary(BigDecimal standardBaseSalary) {
        if (standardBaseSalary == null) {
            return BigDecimal.ZERO;
        }
        return standardBaseSalary.multiply(baseSalaryRatio)
               .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    //判断是否计算该项目
     public boolean shouldCalculateItem(String projectCategory) {
        if (projectCategory == null) {
            return false;
        }
        
        switch (projectCategory) {
            case "基本薪资":
                return true; // 基本薪资要计算，但是按比例
            case "津贴":
                return calculateAllowance;
            case "奖金":
                return calculateBonus;
            case "社保":
                return calculateInsurance;
            case "扣款":
                return true; // 扣款（如个税）要计算
            default:
                return false;
        }
    }
}

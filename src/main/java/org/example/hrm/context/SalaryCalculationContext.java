package org.example.hrm.context;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.example.hrm.entity.User;
import org.example.hrm.policy.ProbationPolicy;

import lombok.Data;

/**
 * 薪酬计算上下文
 */
@Data
public class SalaryCalculationContext {
    //员工信息
    private User employee;
    //薪酬计算月份
    private LocalDate salaryMonth;
    //标准基本工资
    private BigDecimal standardBaseSalary;
    //实际工作天数
    private Integer actualWorkDays;
    //当月总工作天数
    private Integer totalWorkDays;
    //试用期政策
    private ProbationPolicy probationPolicy;
    //是否在试用期
    private boolean inProbation;

    /* 创建薪酬计算上下文 */
    public static SalaryCalculationContext create(User employee,LocalDate salaryMonth,BigDecimal standardBaseSalary)
    {
        SalaryCalculationContext context = new SalaryCalculationContext();
        context.setEmployee(employee);
        context.setSalaryMonth(salaryMonth);
        context.setStandardBaseSalary(standardBaseSalary);

     //初始化试用期政策
        ProbationPolicy policy = new ProbationPolicy();
        context.setProbationPolicy(policy);

        //设置工作天数（默认标准22天）
        context.setTotalWorkDays(22);
        return context;

    }

    /* 获取实际基本工资 */
    public BigDecimal getActualBaseSalary(){
        if (inProbation) {
            return probationPolicy.calculateProbationBaseSalary(standardBaseSalary);
        }
        return standardBaseSalary;
    }

    /* 判断是否计算该项目 */
    public boolean shouldCalculateItem(String projectCode,String projectCategory) {
    if (!probationPolicy.shouldCalculateItem(projectCategory )) {
        return false;
    }
    //特殊项目：试用期没有社保
    if (inProbation && "社保".equals(projectCategory)) {
        return false;
    }

    return true;

    }

    /* 计算日工资（用于离职） */
    public BigDecimal getDailySalary(){
        BigDecimal baseSalary = getActualBaseSalary();
        if (totalWorkDays == null || totalWorkDays == 0) {
            return BigDecimal.ZERO;
        }

        return baseSalary.divide(new BigDecimal(totalWorkDays) , 2, java.math.RoundingMode.HALF_UP);
    }

    /* 计算应发工资 */
    public BigDecimal calculatePayableSalary(){
        BigDecimal dailySalary = getDailySalary();
        Integer workDays = actualWorkDays != null ? actualWorkDays : totalWorkDays;
        return dailySalary.multiply(new BigDecimal(workDays)).setScale(2,java.math.RoundingMode.HALF_UP);
    }
}



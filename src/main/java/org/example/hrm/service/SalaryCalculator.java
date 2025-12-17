package org.example.hrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hrm.common.BusinessException;
import org.example.hrm.common.ResultCode;
import org.example.hrm.context.SalaryCalculationContext;
import org.example.hrm.entity.SalaryProject;
import org.example.hrm.entity.User;
import org.example.hrm.repository.UserRepository;
import org.example.hrm.strategy.SalaryCalculationStrategy;
import org.example.hrm.strategy.SalaryCalculationStrategyFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪酬计算服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SalaryCalculator {
    
    private final UserRepository userRepository;
    private final SalaryCalculationStrategyFactory strategyFactory;
    private final SalaryProjectService salaryProjectService;
    
    /**
     * 计算员工薪酬
     */
    public SalaryCalculationResult calculateEmployeeSalary(Long userId, LocalDate salaryMonth) {
        // 获取员工信息
        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST));
        
        // 获取员工基本工资配置
        BigDecimal standardBaseSalary = getEmployeeBaseSalary(employee);
        
        // 创建计算上下文
        SalaryCalculationContext context = SalaryCalculationContext.create(
                employee, salaryMonth, standardBaseSalary);
        
        // 获取所有启用的薪酬项目
        List<SalaryProject> projects = salaryProjectService.getEnabledProjects();
        
        // 初始化结果
        SalaryCalculationResult result = new SalaryCalculationResult();
        result.setEmployeeId(userId);
        result.setEmployeeName(employee.getUserName());
        result.setEmployeeCode(employee.getUserCode());
        result.setSalaryMonth(salaryMonth);
        result.setInProbation(context.isInProbation());
        result.setWorkDays(context.getActualWorkDays());
        result.setTotalDays(context.getTotalWorkDays());
        result.setStandardBaseSalary(context.getStandardBaseSalary());
        result.setActualBaseSalary(context.getActualBaseSalary());
        
        // 计算各薪酬项目
        List<SalaryItem> salaryItems = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalDeduction = BigDecimal.ZERO;
        
        for (SalaryProject project : projects) {
            SalaryItem item = calculateSalaryItem(project, context);
            if (item != null) {
                salaryItems.add(item);
                
                // 统计总额
                if (project.getProjectType() == 1) { // 1-增加项
                    totalIncome = totalIncome.add(item.getAmount());
                } else { // 2-扣除项
                    totalDeduction = totalDeduction.add(item.getAmount());
                }
            }
        }
        
        // 计算个人所得税
        SalaryItem taxItem = calculatePersonalIncomeTax(totalIncome, context);
        if (taxItem != null) {
            salaryItems.add(taxItem);
            totalDeduction = totalDeduction.add(taxItem.getAmount());
        }
        
        // 设置结果
        result.setSalaryItems(salaryItems);
        result.setTotalIncome(totalIncome);
        result.setTotalDeduction(totalDeduction);
        result.setActualAmount(totalIncome.subtract(totalDeduction));
        
        // 记录日志
        logCalculationDetails(result);
        
        return result;
    }
    
    /**
     * 计算单个薪酬项目
     */
    private SalaryItem calculateSalaryItem(SalaryProject project, SalaryCalculationContext context) {
        // 检查是否应该计算项目
        if (!shouldCalculateItem(project, context)) {
            log.debug("跳过项目：{}，类别：{}，试用期：{}",
                    project.getProjectName(), project.getCategory(), context.isInProbation());
            return null;
        }
        
        try {
            // 获取计算策略
            SalaryCalculationStrategy strategy = strategyFactory.getStrategy(project.getCalculationMethod());
            if (strategy == null) {
                log.warn("未找到计算策略：{}", project.getCalculationMethod());
                return null;
            }
            
            // 准备计算参数
            Map<String, Object> params = prepareCalculationParams(project, context);
            
            // 计算金额
            BigDecimal amount = strategy.calculate(context, params);
            
            // 创建结果项
            SalaryItem item = new SalaryItem();
            item.setProjectId(project.getProjectId());
            item.setProjectCode(project.getProjectCode());
            item.setProjectName(project.getProjectName());
            item.setProjectType(project.getProjectType());
            item.setCategory(project.getCategory());
            item.setCalculationMethod(project.getCalculationMethod());
            item.setAmount(amount);
            item.setRemark(getItemRemark(project, context, amount));
            
            return item;
            
        } catch (Exception e) {
            log.error("计算薪酬项目失败：{}，错误：{}", project.getProjectCode(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 判断是否应该计算项目
     */
    private boolean shouldCalculateItem(SalaryProject project, SalaryCalculationContext context) {
        // 试用期不计算社保
        if (context.isInProbation() && "社保".equals(project.getCategory())) {
            return false;
        }
        
        // 离职员工不计算奖金和津贴
        if (context.isResignedEmployee() && 
            ("奖金".equals(project.getCategory()) || "津贴".equals(project.getCategory()))) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 准备计算参数
     */
    private Map<String, Object> prepareCalculationParams(SalaryProject project, SalaryCalculationContext context) {
        Map<String, Object> params = new HashMap<>();
        
        // 根据项目编码设置不同的参数
        switch (project.getProjectCode()) {
            case "BASE_SALARY":
                // 基本工资使用实际基本工资
                break;
                
            case "FULL_ATTENDANCE_BONUS":
                // 全勤奖：如果有缺勤则为0，否则为固定值
                if (context.getActualWorkDays() >= context.getTotalWorkDays()) {
                    params.put("fixedAmount", new BigDecimal("200"));
                } else {
                    params.put("fixedAmount", BigDecimal.ZERO);
                }
                break;
                
            case "TRANSPORT_ALLOWANCE":
            case "MEAL_ALLOWANCE":
            case "COMMUNICATION_ALLOWANCE":
                // 津贴：固定值
                params.put("fixedAmount", getAllowanceAmount(project.getProjectCode()));
                break;
                
            case "PENSION_INSURANCE":
            case "MEDICAL_INSURANCE":
            case "HOUSING_FUND":
            case "UNEMPLOYMENT_INSURANCE":
                // 社保：百分比计算，基于标准基本工资
                params.put("percentage", getInsurancePercentage(project.getProjectCode()));
                params.put("baseType", "standardBaseSalary");
                break;
                
            case "OVERTIME_PAY":
                // 加班费：公式计算
                params.put("formula", "${baseSalary} / ${totalDays} / 8 * ${overtimeHours} * 1.5");
                params.put("overtimeHours", 10); // 假设加班10小时
                break;
                
            case "ABSENCE_DEDUCTION":
                // 缺勤扣款：公式计算
                int absenceDays = context.getTotalWorkDays() - context.getActualWorkDays();
                params.put("formula", "${baseSalary} / ${totalDays} * " + absenceDays);
                break;
                
            case "PERFORMANCE_BONUS":
                // 绩效奖金：根据绩效等级阶梯计算
                Map<String, BigDecimal> stepRates = new HashMap<>();
                stepRates.put("A", new BigDecimal("0.3"));
                stepRates.put("B", new BigDecimal("0.2"));
                stepRates.put("C", new BigDecimal("0.1"));
                stepRates.put("D", new BigDecimal("0"));
                
                params.put("performanceLevel", "B"); // 假设绩效等级为B
                params.put("stepRates", stepRates);
                break;
        }
        
        return params;
    }
    
    /**
     * 计算个人所得税
     */
    private SalaryItem calculatePersonalIncomeTax(BigDecimal totalIncome, SalaryCalculationContext context) {
        // 计算应纳税所得额（起征点5000）
        BigDecimal taxableIncome = totalIncome.subtract(new BigDecimal("5000"));
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        // 计算个税（简化版）
        BigDecimal tax = calculateTax(taxableIncome);
        
        if (tax.compareTo(BigDecimal.ZERO) > 0) {
            SalaryItem item = new SalaryItem();
            item.setProjectCode("PERSONAL_INCOME_TAX");
            item.setProjectName("个人所得税");
            item.setProjectType(2); // 扣除项
            item.setCategory("扣款");
            item.setCalculationMethod("公式计算");
            item.setAmount(tax);
            item.setRemark("累计预扣法计算个人所得税");
            return item;
        }
        
        return null;
    }
    
    /**
     * 计算个税
     */
    private BigDecimal calculateTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(new BigDecimal("3000")) <= 0) {
            return taxableIncome.multiply(new BigDecimal("0.03"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (taxableIncome.compareTo(new BigDecimal("12000")) <= 0) {
            return taxableIncome.multiply(new BigDecimal("0.1"))
                    .subtract(new BigDecimal("210"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (taxableIncome.compareTo(new BigDecimal("25000")) <= 0) {
            return taxableIncome.multiply(new BigDecimal("0.2"))
                    .subtract(new BigDecimal("1410"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            return taxableIncome.multiply(new BigDecimal("0.3"))
                    .subtract(new BigDecimal("2660"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
    
    /**
     * 获取员工基本工资
     */
    private BigDecimal getEmployeeBaseSalary(User employee) {
        // 实际应该从薪酬配置表中查询
        // 这里使用简化处理
        if (employee.getPosId() != null) {
            // 根据职位设置不同的基本工资
            switch (employee.getPosId().intValue()) {
                case 1: return new BigDecimal("10000"); // 经理
                case 2: return new BigDecimal("8000");  // 主管
                case 3: return new BigDecimal("6000");  // 专员
                default: return new BigDecimal("5000"); // 普通员工
            }
        }
        return new BigDecimal("5000");
    }
    
    /**
     * 获取津贴金额
     */
    private BigDecimal getAllowanceAmount(String projectCode) {
        switch (projectCode) {
            case "TRANSPORT_ALLOWANCE": return new BigDecimal("300");
            case "MEAL_ALLOWANCE": return new BigDecimal("500");
            case "COMMUNICATION_ALLOWANCE": return new BigDecimal("200");
            default: return BigDecimal.ZERO;
        }
    }
    
    /**
     * 获取社保比例
     */
    private BigDecimal getInsurancePercentage(String projectCode) {
        switch (projectCode) {
            case "PENSION_INSURANCE": return new BigDecimal("8");  // 8%
            case "MEDICAL_INSURANCE": return new BigDecimal("2");  // 2%
            case "HOUSING_FUND": return new BigDecimal("5");      // 5%
            case "UNEMPLOYMENT_INSURANCE": return new BigDecimal("0.5"); // 0.5%
            default: return BigDecimal.ZERO;
        }
    }
    
    /**
     * 获取项目备注
     */
    private String getItemRemark(SalaryProject project, SalaryCalculationContext context, BigDecimal amount) {
        StringBuilder remark = new StringBuilder();
        
        if (context.isInProbation()) {
            remark.append("试用期");
        }
        
        if (context.isResignedEmployee()) {
            remark.append("离职结算");
        }
        
        if ("固定值".equals(project.getCalculationMethod())) {
            remark.append("固定金额计算");
        } else if ("百分比".equals(project.getCalculationMethod())) {
            remark.append("百分比计算");
        } else if ("公式计算".equals(project.getCalculationMethod())) {
            remark.append("公式计算");
        }
        
        return remark.toString();
    }
    
    /**
     * 记录计算明细
     */
    private void logCalculationDetails(SalaryCalculationResult result) {
        log.info("薪酬计算完成 - 员工: {} ({})，月份: {}，试用期: {}，工作天数: {}/{}，应发: {}，实发: {}",
                result.getEmployeeName(),
                result.getEmployeeCode(),
                result.getSalaryMonth(),
                result.isInProbation() ? "是" : "否",
                result.getWorkDays(),
                result.getTotalDays(),
                result.getTotalIncome(),
                result.getActualAmount());
    }
}

/**
 * 薪酬计算结果
 */
class SalaryCalculationResult {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate salaryMonth;
    private boolean inProbation;
    private int workDays;
    private int totalDays;
    private BigDecimal standardBaseSalary;
    private BigDecimal actualBaseSalary;
    private List<SalaryItem> salaryItems;
    private BigDecimal totalIncome;
    private BigDecimal totalDeduction;
    private BigDecimal actualAmount;
    
    // getters and setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public LocalDate getSalaryMonth() { return salaryMonth; }
    public void setSalaryMonth(LocalDate salaryMonth) { this.salaryMonth = salaryMonth; }
    
    public boolean isInProbation() { return inProbation; }
    public void setInProbation(boolean inProbation) { this.inProbation = inProbation; }
    
    public int getWorkDays() { return workDays; }
    public void setWorkDays(int workDays) { this.workDays = workDays; }
    
    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
    
    public BigDecimal getStandardBaseSalary() { return standardBaseSalary; }
    public void setStandardBaseSalary(BigDecimal standardBaseSalary) { this.standardBaseSalary = standardBaseSalary; }
    
    public BigDecimal getActualBaseSalary() { return actualBaseSalary; }
    public void setActualBaseSalary(BigDecimal actualBaseSalary) { this.actualBaseSalary = actualBaseSalary; }
    
    public List<SalaryItem> getSalaryItems() { return salaryItems; }
    public void setSalaryItems(List<SalaryItem> salaryItems) { this.salaryItems = salaryItems; }
    
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    
    public BigDecimal getTotalDeduction() { return totalDeduction; }
    public void setTotalDeduction(BigDecimal totalDeduction) { this.totalDeduction = totalDeduction; }
    
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
}

/**
 * 薪酬项目计算结果
 */
class SalaryItem {
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Integer projectType;
    private String category;
    private String calculationMethod;
    private BigDecimal amount;
    private String remark;
    
    // getters and setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public Integer getProjectType() { return projectType; }
    public void setProjectType(Integer projectType) { this.projectType = projectType; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
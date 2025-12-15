package org.example.hrm.constant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪酬管理常量类
 * 统一管理系统中所有的常量值
 */
public class SalaryConstants {

    /* 试用期常量 */
    public static final int PROBATION_PERIOD_MONTHS = 3;  // 试用期3个月
    public static final BigDecimal PROBATION_SALARY_RATIO = new BigDecimal("0.5"); // 试用期工资比例
    public static final boolean PROBATION_HAS_ALLOWANCE = true;  // 试用期有津贴
    public static final boolean PROBATION_HAS_BONUS = true;      // 试用期有奖金
    public static final boolean PROBATION_HAS_INSURANCE = false; // 试用期无社保

    /* 项目类型常量 */
    public static final int  PROJECT_TYPE_INCOME = 1; // 收入类项目
    public static final int  PROJECT_TYPE_DEDUCTION = 2; // 扣款类项目

    public static final String PROJECT_TYPE_INCOME_STR = "增项";
    public static final String PROJECT_TYPE_DEDUCTION_STR = "扣项";

    /* 项目类别常量 */
    public static final String CATEGORY_BASE_SALARY = "基本工资"; // 基本工资
    public static final String CATEGORY_BONUS = "奖金"; // 奖金
    public static final String CATEGORY_ALLOWANCE = "补贴"; // 补贴
    public static final String CATEGORY_DEDUCTION = "扣款"; // 扣款
    public static final String CATEGORY_INSURANCE = "社保"; // 社保

     // 试用期需要排除的社保项目
    public static final List<String> INSURANCE_PROJECTS = Arrays.asList(
        "PENSION_INSURANCE",    // 养老保险
        "MEDICAL_INSURANCE",    // 医疗保险
        "UNEMPLOYMENT_INSURANCE", // 失业保险
        "INJURY_INSURANCE",     // 工伤保险
        "MATERNITY_INSURANCE",  // 生育保险
        "HOUSING_FUND"          // 住房公积金
    );
    /**
     * 判断是否为社保项目
     */
    public static boolean isInsuranceProject(String projectCode) {
        return INSURANCE_PROJECTS.contains(projectCode);
    }
    
    /**
     * 判断是否为基本薪资项目
     */
    public static boolean isBaseSalaryProject(String projectCode) {
        return "BASE_SALARY".equals(projectCode);
    }

    /* 计算方式常量 */
    public static final String CALC_METHOD_FIXED = "固定值"; // 固定金额
    public static final String CALC_METHOD_PERCENTAGE = "百分比"; // 百分比
    public static final String CALC_METHOD_FORMULA = "公式计算"; // 公式计算

    /* 薪酬标准状态常量 */
    public static final int  STANDARD_STATUS_DRAFT = 1; // 草稿
    public static final int  STANDARD_STATUS_PENDING = 2; // 待审核
    public static final int  STANDARD_STATUS_APPROVED = 3; // 审核通过
    public static final int  STANDARD_STATUS_REJECTED = 0; // 审核不通过

    public static final String STANDARD_STATUS_DRAFT_STR = "草稿";
    public static final String STANDARD_STATUS_PENDING_STR = "待审核";
    public static final String STANDARD_STATUS_APPROVED_STR = "已生效";
    public static final String STANDARD_STATUS_REJECTED_STR = "驳回";

    /* 薪酬发放状态常量 */
    public static final int  PAYMENT_STATUS_DRAFT = 1; // 草稿
    public static final int PAYMENT_STATUS_PENDING = 2; // 待复核
    public static final int PAYMENT_STATUS_PAID = 3; // 已发放
    public static final int PAYMENT_STATUS_REJECTED = 0; // 驳回

    public static final String PAYMENT_STATUS_DRAFT_STR = "草稿";
    public static final String PAYMENT_STATUS_PENDING_STR = "待审核";
    public static final String PAYMENT_STATUS_PAID_STR = "已发放";
    public static final String PAYMENT_STATUS_REJECTED_STR = "驳回";


    /* 员工状态 */
    public static final int EMPLOYEE_STATUS_ACTIVE = 1; // 在职
    public static final int EMPLOYEE_STATUS_RESIGNED = 0; // 离职
    public static final int EMPLOYEE_STATUS_DISABLED = 2; // 停用

    public static final String EMPLOYEE_STATUS_ACTIVE_STR = "在职";
    public static final String EMPLOYEE_STATUS_RESIGNED_STR = "离职";
    public static final String EMPLOYEE_STATUS_DISABLED_STR = "停用";

    /* 机构状态 */
    public static final int ORG_STATUS_ENABLED = 1; // 启用
    public static final int ORG_STATUS_DISABLED = 2; // 停用

    public static final String ORG_STATUS_ENABLED_STR = "启用";
    public static final String ORG_STATUS_DISABLED_STR = "停用";

    /* 角色状态常量 */
     public static final int ROLE_STATUS_ENABLED = 1;        // 启用
    public static final int ROLE_STATUS_DISABLED = 0;       // 禁用
    
    public static final String ROLE_STATUS_ENABLED_STR = "启用";
    public static final String ROLE_STATUS_DISABLED_STR = "禁用";

    /* 标准项目编码常量 */
     public static final String PROJECT_CODE_BASE_SALARY = "BASE_SALARY";         // 基本工资
    public static final String PROJECT_CODE_PERFORMANCE_BONUS = "PERFORMANCE_BONUS"; // 绩效奖金
    public static final String PROJECT_CODE_TRANSPORT_ALLOWANCE = "TRANSPORT_ALLOWANCE"; // 交通补贴
    public static final String PROJECT_CODE_MEAL_ALLOWANCE = "MEAL_ALLOWANCE";   // 餐费补贴
    public static final String PROJECT_CODE_PENSION_INSURANCE = "PENSION_INSURANCE"; // 养老保险
    public static final String PROJECT_CODE_MEDICAL_INSURANCE = "MEDICAL_INSURANCE"; // 医疗保险
    public static final String PROJECT_CODE_UNEMPLOYMENT_INSURANCE = "UNEMPLOYMENT_INSURANCE"; // 失业保险
    public static final String PROJECT_CODE_HOUSING_FUND = "HOUSING_FUND";       // 住房公积金
    public static final String PROJECT_CODE_PERSONAL_INCOME_TAX = "PERSONAL_INCOME_TAX"; // 个人所得税

    /* 常用数组常量 */
     public static final int DEFAULT_WORK_DAYS_PER_MONTH = 22;     // 每月标准工作天数
    public static final BigDecimal DEFAULT_TAX_THRESHOLD = new BigDecimal("5000");  // 个税起征点
    public static final BigDecimal DEFAULT_PENSION_RATIO = new BigDecimal("0.08");  // 养老保险个人比例
    public static final BigDecimal DEFAULT_MEDICAL_RATIO = new BigDecimal("0.02");  // 医疗保险个人比例
    public static final BigDecimal DEFAULT_UNEMPLOYMENT_RATIO = new BigDecimal("0.005");  // 失业保险个人比例
    public static final BigDecimal DEFAULT_HOUSING_FUND_RATIO = new BigDecimal("0.05");   // 住房公积金个人比例（5-12%）

    /* 薪酬标准关联表名 */
      public static final String TABLE_STANDARD_POSITION = "standard_position";   // 标准职位关联表
    public static final String TABLE_STANDARD_ITEM = "standard_item";           // 标准明细表
    
     /**
     * 根据项目类型代码获取项目类型名称
     * 
     * @param typeCode 项目类型代码
     * @return 项目类型名称
     */
    public static String getProjectTypeName(Integer typeCode) {
        if (typeCode == null) return "未知";
        return typeCode.equals(PROJECT_TYPE_INCOME) ? PROJECT_TYPE_INCOME_STR : PROJECT_TYPE_DEDUCTION_STR;
    }

     /**
     * 根据薪酬标准状态代码获取状态名称
     * 
     * @param statusCode 状态代码
     * @return 状态名称
     */
    public static String getStandardStatusName(Integer statusCode) {
        if (statusCode == null) return "未知";
        switch (statusCode) {
            case STANDARD_STATUS_DRAFT:
                return STANDARD_STATUS_DRAFT_STR;
            case STANDARD_STATUS_PENDING:
                return STANDARD_STATUS_PENDING_STR;
            case STANDARD_STATUS_APPROVED:
                return STANDARD_STATUS_APPROVED_STR;
            case STANDARD_STATUS_REJECTED:
                return STANDARD_STATUS_REJECTED_STR;
            default:
                return "未知";
        }
    }

 /**
     * 根据薪酬发放状态代码获取状态名称
     * 
     * @param statusCode 状态代码
     * @return 状态名称
     */
    public static String getPaymentStatusName(Integer statusCode) {
        if (statusCode == null) return "未知";
        
        switch (statusCode) {
            case PAYMENT_STATUS_DRAFT:
                return PAYMENT_STATUS_DRAFT_STR;
            case PAYMENT_STATUS_PENDING:
                return PAYMENT_STATUS_PENDING_STR;
            case PAYMENT_STATUS_PAID:
                return PAYMENT_STATUS_PAID_STR;
            case PAYMENT_STATUS_REJECTED:
                return PAYMENT_STATUS_REJECTED_STR;
            default:
                return "未知";
        }
    }

     /**
     * 根据员工状态代码获取状态名称
     * 
     * @param statusCode 员工状态代码
     * @return 状态名称
     */
    public static String getEmployeeStatusName(Integer statusCode) {
        if (statusCode == null) return "未知";
        
        switch (statusCode) {
            case EMPLOYEE_STATUS_ACTIVE:
                return EMPLOYEE_STATUS_ACTIVE_STR;
            case EMPLOYEE_STATUS_DISABLED:
                return EMPLOYEE_STATUS_DISABLED_STR;
            case EMPLOYEE_STATUS_RESIGNED:
                return EMPLOYEE_STATUS_RESIGNED_STR;
            default:
                return "未知";
        }
    }

     /**
     * 根据机构状态代码获取状态名称
     * 
     * @param statusCode 机构状态代码
     * @return 状态名称
     */
    public static String getOrgStatusName(Integer statusCode) {
        if (statusCode == null) return "未知";
        
        switch (statusCode) {
            case ORG_STATUS_ENABLED:
                return ORG_STATUS_ENABLED_STR;
            case ORG_STATUS_DISABLED:
                return ORG_STATUS_DISABLED_STR;
            default:
                return "未知";
        }
    }

     /**
     * 根据角色状态代码获取状态名称
     * 
     * @param statusCode 角色状态代码
     * @return 状态名称
     */
    public static String getRoleStatusName(Integer statusCode) {
        if (statusCode == null) return "未知";
        
        switch (statusCode) {
            case ROLE_STATUS_ENABLED:
                return ROLE_STATUS_ENABLED_STR;
            case ROLE_STATUS_DISABLED:
                return ROLE_STATUS_DISABLED_STR;
            default:
                return "未知";
        }
    }
    
     /**
     * 判断员工是否为活跃、禁用、离职状态
     * 
     * @param status 员工状态
     * @return 是否为xx状态
     */
    public static boolean isEmployeeActive(Integer status) {
        return EMPLOYEE_STATUS_ACTIVE == status;
    }

     public static boolean isEmployeeDisabled(Integer status) {
        return EMPLOYEE_STATUS_DISABLED == status;
    }
    
     public static boolean isEmployeeResigned(Integer status) {
        return EMPLOYEE_STATUS_RESIGNED == status;
    }

     /**
     * 判断是否为增项、减项项目
     * 
     * @param projectType 项目类型
     * @return 是否为x项
     */
    public static boolean isIncomeProject(Integer projectType) {
        return PROJECT_TYPE_INCOME == projectType;
    }
    
    public static boolean isDeductionProject(Integer projectType) {
        return PROJECT_TYPE_DEDUCTION == projectType;
    }

    /**
     * 获取员工状态列表
     * 
     * @return 员工状态列表
     */
    public static List<Map<String, Object>> getEmployeeStatusList() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> active = new HashMap<>();
        active.put("code", EMPLOYEE_STATUS_ACTIVE);
        active.put("name", EMPLOYEE_STATUS_ACTIVE_STR);
        list.add(active);
        
        Map<String, Object> disabled = new HashMap<>();
        disabled.put("code", EMPLOYEE_STATUS_DISABLED);
        disabled.put("name", EMPLOYEE_STATUS_DISABLED_STR);
        list.add(disabled);
        
        Map<String, Object> resigned = new HashMap<>();
        resigned.put("code", EMPLOYEE_STATUS_RESIGNED);
        resigned.put("name", EMPLOYEE_STATUS_RESIGNED_STR);
        list.add(resigned);
        
        return list;
    }


}


package org.example.hrm.enums;
/**
 * 薪酬管理枚举类
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import lombok.Getter;

public class SalaryEnums {
    //项目类型枚举
    @Getter
    public enum ProjectType{
        INCOME(1,"增项"),
        DEDUCTION(2,"减项");

        private final int code;
        private final String name;

        ProjectType(int code, String name){
            this.code = code;
            this.name = name;
        }

        private static String getNameByCode(Integer code){
            if (code == null) {
                return "未知";

            }

            for(ProjectType type : ProjectType.values()){
                if (type.getCode() == code) {
                    return type.getName();
                }
            }
            return "未知";
        }

        public static List<Map<String,Object>> toList(){
            return Arrays.stream(ProjectType.values())
                    .map(type ->{ Map<String,Object> map = new HashMap<>();
                    map.put("code", type.getCode());
                    map.put("name", type.getName());
                    return map;
                    })
                    .collect(Collectors.toList());   
        }

    }

     /**
     * 项目类别枚举
     */
    @Getter
    public enum ProjectCategory {
        BASE_SALARY("基本薪资", "固定薪资，按月发放的基本工资"),
        BONUS("奖金", "绩效奖金、年终奖等激励性收入"),
        ALLOWANCE("津贴", "交通补贴、餐补、通讯补贴等"),
        INSURANCE("社保", "养老保险、医疗保险等社会保险费用"),
        DEDUCTION("扣款", "个人所得税、考勤扣款等");
        
        private final String name;
        private final String description;
        
        ProjectCategory(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public static ProjectCategory getByName(String name) {
            for (ProjectCategory category : ProjectCategory.values()) {
                if (category.getName().equals(name)) {
                    return category;
                }
            }
            return null;
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(ProjectCategory.values())
                    .map(category -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", category.getName());
                        map.put("description", category.getDescription());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }

     /**
     * 计算方式枚举
     */
    @Getter
    public enum CalculationMethod {
        FIXED("固定值", "直接填写固定金额"),
        PERCENTAGE("百分比", "按基本工资的百分比计算"),
        FORMULA("公式计算", "根据特定公式计算");
        
        private final String name;
        private final String description;
        
        CalculationMethod(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public static CalculationMethod getByName(String name) {
            for (CalculationMethod method : CalculationMethod.values()) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
            return null;
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(CalculationMethod.values())
                    .map(method -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", method.getName());
                        map.put("description", method.getDescription());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 薪酬标准状态枚举
     */
    @Getter
    public enum StandardStatus {
        DRAFT(1, "草稿", "gray"),
        PENDING(2, "待审核", "orange"),
        APPROVED(3, "已生效", "green"),
        REJECTED(0, "驳回", "red");
        
        private final Integer code;
        private final String name;
        private final String color; // 用于前端显示的颜色
        
        StandardStatus(Integer code, String name, String color) {
            this.code = code;
            this.name = name;
            this.color = color;
        }
        
        public static StandardStatus getByCode(Integer code) {
            for (StandardStatus status : StandardStatus.values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
        
        public static String getNameByCode(Integer code) {
            StandardStatus status = getByCode(code);
            return status != null ? status.getName() : "未知";
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(StandardStatus.values())
                    .map(status -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", status.getCode());
                        map.put("name", status.getName());
                        map.put("color", status.getColor());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 薪酬发放状态枚举
     */
    @Getter
    public enum PaymentStatus {
        DRAFT(1, "草稿", "gray"),
        PENDING(2, "待复核", "orange"),
        PAID(3, "已发放", "green"),
        REJECTED(0, "已驳回", "red");
        
        private final Integer code;
        private final String name;
        private final String color;
        
        PaymentStatus(Integer code, String name, String color) {
            this.code = code;
            this.name = name;
            this.color = color;
        }
        
        public static PaymentStatus getByCode(Integer code) {
            for (PaymentStatus status : PaymentStatus.values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
        
        public static String getNameByCode(Integer code) {
            PaymentStatus status = getByCode(code);
            return status != null ? status.getName() : "未知";
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(PaymentStatus.values())
                    .map(status -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", status.getCode());
                        map.put("name", status.getName());
                        map.put("color", status.getColor());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 员工状态枚举
     */
    @Getter
    public enum EmployeeStatus {
        ACTIVE(1, "在职", "green", true),     // 在职，可参与薪酬计算
        DISABLED(2, "禁用", "orange", false),  // 账号禁用，不可参与薪酬计算
        RESIGNED(0, "离职", "red", false);     // 离职，不可参与薪酬计算
        
        private final Integer code;
        private final String name;
        private final String color;
        private final Boolean canCalculate; // 是否可以计算薪酬
        
        EmployeeStatus(Integer code, String name, String color, Boolean canCalculate) {
            this.code = code;
            this.name = name;
            this.color = color;
            this.canCalculate = canCalculate;
        }
        
        public static EmployeeStatus getByCode(Integer code) {
            for (EmployeeStatus status : EmployeeStatus.values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
        
        public static String getNameByCode(Integer code) {
            EmployeeStatus status = getByCode(code);
            return status != null ? status.getName() : "未知";
        }
        
        public static Boolean canCalculateSalary(Integer code) {
            EmployeeStatus status = getByCode(code);
            return status != null ? status.getCanCalculate() : false;
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(EmployeeStatus.values())
                    .map(status -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", status.getCode());
                        map.put("name", status.getName());
                        map.put("color", status.getColor());
                        map.put("canCalculate", status.getCanCalculate());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
        
        public Boolean getCanCalculate() {
            return canCalculate;
        }
    }
    

     /**
     * 机构状态枚举
     */
    @Getter
    public enum OrgStatus {
        ENABLED(1, "启用", "green"),
        DISABLED(2, "禁用", "red");
        
        private final Integer code;
        private final String name;
        private final String color;
        
        OrgStatus(Integer code, String name, String color) {
            this.code = code;
            this.name = name;
            this.color = color;
        }
        
        public static OrgStatus getByCode(Integer code) {
            for (OrgStatus status : OrgStatus.values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
        
        public static String getNameByCode(Integer code) {
            OrgStatus status = getByCode(code);
            return status != null ? status.getName() : "未知";
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(OrgStatus.values())
                    .map(status -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", status.getCode());
                        map.put("name", status.getName());
                        map.put("color", status.getColor());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }

     /**
     * 角色状态枚举
     */
    @Getter
    public enum RoleStatus {
        ENABLED(1, "启用", "green"),
        DISABLED(0, "禁用", "red");
        
        private final Integer code;
        private final String name;
        private final String color;
        
        RoleStatus(Integer code, String name, String color) {
            this.code = code;
            this.name = name;
            this.color = color;
        }
        
        public static RoleStatus getByCode(Integer code) {
            for (RoleStatus status : RoleStatus.values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
        
        public static String getNameByCode(Integer code) {
            RoleStatus status = getByCode(code);
            return status != null ? status.getName() : "未知";
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(RoleStatus.values())
                    .map(status -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", status.getCode());
                        map.put("name", status.getName());
                        map.put("color", status.getColor());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 薪酬项目编码枚举
     */
    @Getter
    public enum ProjectCode {
        // 基本薪资类
        BASE_SALARY("BASE_SALARY", "基本工资", "基本薪资", "固定值", true),
        
        // 奖金类
        FULL_ATTENDANCE_BONUS("FULL_ATTENDANCE_BONUS", "全勤奖", "奖金", "固定值", false),
        OVERTIME_PAY("OVERTIME_PAY", "加班费", "奖金", "公式计算", false),
        PERFORMANCE_BONUS("PERFORMANCE_BONUS", "绩效奖金", "奖金", "固定值", false),
        
        // 津贴类
        TRANSPORT_ALLOWANCE("TRANSPORT_ALLOWANCE", "交通补贴", "津贴", "固定值", false),
        MEAL_ALLOWANCE("MEAL_ALLOWANCE", "餐费补贴", "津贴", "固定值", false),
        COMMUNICATION_ALLOWANCE("COMMUNICATION_ALLOWANCE", "通讯补贴", "津贴", "固定值", false),
        HOUSING_ALLOWANCE("HOUSING_ALLOWANCE", "住房补贴", "津贴", "固定值", false),
        
        // 社保类
        PENSION_INSURANCE("PENSION_INSURANCE", "养老保险", "社保", "百分比", true),
        MEDICAL_INSURANCE("MEDICAL_INSURANCE", "医疗保险", "社保", "百分比", true),
        UNEMPLOYMENT_INSURANCE("UNEMPLOYMENT_INSURANCE", "失业保险", "社保", "百分比", true),
        INJURY_INSURANCE("INJURY_INSURANCE", "工伤保险", "社保", "百分比", true),
        MATERNITY_INSURANCE("MATERNITY_INSURANCE", "生育保险", "社保", "百分比", true),
        HOUSING_FUND("HOUSING_FUND", "住房公积金", "社保", "百分比", true),
        
        // 扣款类
        PERSONAL_INCOME_TAX("PERSONAL_INCOME_TAX", "个人所得税", "扣款", "公式计算", true),
        LATE_DEDUCTION("LATE_DEDUCTION", "迟到扣款", "扣款", "固定值", false),
        ABSENCE_DEDUCTION("ABSENCE_DEDUCTION", "缺勤扣款", "扣款", "公式计算", false),
        OTHER_DEDUCTION("OTHER_DEDUCTION", "其他扣款", "扣款", "固定值", false);
        
        private final String code;
        private final String name;
        private final String category;
        private final String calculationMethod;
        private final Boolean isRequired; // 是否必填项目
        
        ProjectCode(String code, String name, String category, String calculationMethod, Boolean isRequired) {
            this.code = code;
            this.name = name;
            this.category = category;
            this.calculationMethod = calculationMethod;
            this.isRequired = isRequired;
        }
        
        public static ProjectCode getByCode(String code) {
            for (ProjectCode projectCode : ProjectCode.values()) {
                if (projectCode.getCode().equals(code)) {
                    return projectCode;
                }
            }
            return null;
        }
        
        public static String getNameByCode(String code) {
            ProjectCode projectCode = getByCode(code);
            return projectCode != null ? projectCode.getName() : "未知项目";
        }
        
        public static Boolean isRequiredProject(String code) {
            ProjectCode projectCode = getByCode(code);
            return projectCode != null && projectCode.getIsRequired();
        }
        
        public static List<ProjectCode> getRequiredProjects() {
            return Arrays.stream(ProjectCode.values())
                    .filter(ProjectCode::getIsRequired)
                    .collect(Collectors.toList());
        }
        
        public static List<Map<String, Object>> toList() {
            return Arrays.stream(ProjectCode.values())
                    .map(project -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", project.getCode());
                        map.put("name", project.getName());
                        map.put("category", project.getCategory());
                        map.put("calculationMethod", project.getCalculationMethod());
                        map.put("isRequired", project.getIsRequired());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
        
        public Boolean getIsRequired() {
            return isRequired;
        }
    }


}

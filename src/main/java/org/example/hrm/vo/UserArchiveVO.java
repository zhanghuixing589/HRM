package org.example.hrm.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserArchiveVO {
      // 用户表字段
    private Long userId;
    private String userCode;
    private String userName;
    private String email;
    private String phone;
    private Long  orgId;
    private String orgName;
    private Long  posId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer roleType;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private Long archiveId;
    
    // 档案表字段
    private Long  arcId;
    private String arcCode;
    private String name;
    private Integer sex;
    private String idCard;
    private Long  firstOrgId;
    private String firstOrgName;
    private Long  secondOrgId;
    private String secondOrgName;
    private Long  thirdOrgId;
    private String thirdOrgName;
    private String positionName;
    private String title;
    private Long  salaryStandard;
    private LocalDate birDate;
    private String nationality;
    private String qualification;
    private String photoPath;
    private String telephone;
    private String address;
    private String zipCode;
    private String country;
    private String birAddress;
    private String belief;
    private String identity;
    private String major;
    private Integer archiveStatus;
    private String reason;
    private Integer resubmitCount;
    
    // 转换方法
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "在职";
            case 0: return "离职";
            case 2: return "禁用";
            default: return "未知";
        }
    }
    
    public String getSexText() {
        if (sex == null) return "未知";
        return sex == 1 ? "男" : sex == 2 ? "女" : "未知";
    }
    
    public String getArchiveStatusText() {
        if (archiveStatus == null) return "未知";
        switch (archiveStatus) {
            case 1: return "待复核";
            case 2: return "已通过";
            case 3: return "已驳回";
            case 4: return "重新提交待审核";
            case 0: return "已删除";
            default: return "未知";
        }
    }
    
    public String getRoleText() {
        if (roleType == null) return "用户";
        switch (roleType) {
            case 1: return "系统管理员";
            case 2: return "人事经理";
            case 3: return "薪酬经理";
            case 4: return "人事专员";
            case 5: return "薪酬专员";
            case 6: return "普通员工";
            default: return "用户";
        }
    }

}

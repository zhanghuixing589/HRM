package org.example.hrm.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/* 员工视图对象 */
@Data
public class UserVO {
    private Long userId;
    private String userCode;
    private String userName;
    private String email;
    private String phone;

     private Long orgId;
    private String orgName;
    
    private Long posId;
    private String positionName;
    
    private Integer roleType;
    private String roleName;
    
    private Integer status;
    private String statusName;

     private LocalDate entryDate;
    private LocalDate leaveDate;
    
    private Integer workYears; // 工作年限
    private Boolean canCalculateSalary; // 是否可以计算薪酬
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}

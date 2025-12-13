package org.example.hrm.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/* 员工数据传输对象 */
@Data
public class UserDTO {
    private Long userId;

    @NotBlank(message = "工号不能为空")
    private String userCode; // 工号

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String email;
    private String phone;

    @NotBlank(message = "组织ID不能为空")
    private Long orgId;

    @NotBlank(message = "职位ID不能为空")
    private Long posId;

    @NotBlank(message = "角色类型不能为空")
    private Integer roleType; // 角色类型（1-系统管理员，2-人事经理...）

    @NotBlank(message = "入职日期不能为空")
    private String entryDate; // 入职日期

    private String leaveDate; // 离职日期（离职状态时填写）

    private Integer status = 1; // 1-在职，0-离职，2-警用

    
}

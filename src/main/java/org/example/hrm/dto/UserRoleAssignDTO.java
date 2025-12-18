package org.example.hrm.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class UserRoleAssignDTO {
  @NotNull(message = "用户ID不能为空")
  private Long userId;

  @NotNull(message = "角色类型不能为空")
  private Integer roleType; // 2-人事经理, 3-薪酬经理, 4-人事专员, 5-薪酬专员, 6-普通员工
}
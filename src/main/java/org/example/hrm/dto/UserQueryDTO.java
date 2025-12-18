package org.example.hrm.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
  private String userCode; // 工号
  private String userName; // 姓名
  private Long posId; // 职位ID
  private Integer roleType; // 角色类型
  private Integer status; // 状态
}
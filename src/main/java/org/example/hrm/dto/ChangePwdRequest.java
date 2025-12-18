package org.example.hrm.dto;

import lombok.Data;

@Data
public class ChangePwdRequest {
  private String username;
  private String oldPassword;
  private String newPassword;
}
package org.example.hrm.vo;

import lombok.Data;

@Data
public class PositionVO {
  private Long  posId;
    private String posCode;
    private String posName;
    private Long  orgId;
    private String orgName; 
    private Integer status;
}

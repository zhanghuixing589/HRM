package org.example.hrm.vo;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;
@Data
@Builder

public class StandardItemVO {
  private Long id;
    private String projectCode;
    private String projectName;
    private Integer projectType;
    private String category;
    
    private String calculationMethod; // 快照值
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private BigDecimal ratio;
    private Integer sortOrder;
}

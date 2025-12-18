package org.example.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardItemProjectionDTO {
    private Long id;
    private String projectCode;
    private String projectName;
    private Integer projectType;
    private String category;
    private String calculationMethod;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private BigDecimal ratio;
    private Integer sortOrder;
}
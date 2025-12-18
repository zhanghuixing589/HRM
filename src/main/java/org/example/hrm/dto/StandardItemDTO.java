package org.example.hrm.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardItemDTO {
     @NotBlank(message = "项目编码不能为空")
    private String projectCode;
    
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private BigDecimal ratio;
    private String calculationMethod;
    private Integer sortOrder;
}

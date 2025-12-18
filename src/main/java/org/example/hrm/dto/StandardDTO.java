package org.example.hrm.dto;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class StandardDTO {
     @NotBlank(message = "标准编码不能为空")
    private String standardCode;

    @NotBlank(message = "标准名称不能为空")
    private String standardName;

    @NotEmpty(message = "请选择职位等级")
    private List<String> levels;

    private Long creatorId;
    private List<StandardItemDTO> items;
    private List<Long> positionIds; 

     
}



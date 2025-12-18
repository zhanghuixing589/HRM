package org.example.hrm.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 创建薪酬项目的DTO
 */
@Data
public class ProjectCreateDTO {
    
    @NotBlank(message = "项目编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "项目编码只能包含大写字母、数字和下划线")
    @Size(max = 50, message = "项目编码长度不能超过50个字符")
    private String projectCode;
    
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    private String projectName;
    
    @NotNull(message = "项目类型不能为空")
    private Integer projectType;
    
    @NotBlank(message = "项目类别不能为空")
    @Size(max = 20, message = "项目类别长度不能超过20个字符")
    private String category;
    
    @NotBlank(message = "计算方式不能为空")
    @Size(max = 20, message = "计算方式长度不能超过20个字符")
    private String calculationMethod;
    
    private Integer sortOrder = 0;
    
    private Integer status = 1;
    
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    // 计算参数，JSON字符串格式
    private String params;
}
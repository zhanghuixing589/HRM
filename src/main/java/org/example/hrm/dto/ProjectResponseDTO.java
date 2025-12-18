package org.example.hrm.dto;

import lombok.Data;
import java.util.Date;

/**
 * 薪酬项目响应DTO
 */
@Data
public class ProjectResponseDTO {
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Integer projectType;
    private String projectTypeName;
    private String category;
    private String calculationMethod;
    private Integer sortOrder;
    private Integer status;
    private String statusName;
    private String description;
    private Date createdAt;
    private String params;
}
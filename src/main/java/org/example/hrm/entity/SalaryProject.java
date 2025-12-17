package org.example.hrm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/* 薪酬项目 */
@Data
@Entity
@Table(name = "project")

public class SalaryProject {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @Column(name = "project_code",unique = true, nullable = false)
    private String projectCode;
    
    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "project_type", nullable = false)
    private Integer projectType; // 1-增加项，2-扣除项

    @Column(name = "category", nullable = false)
    private String category; // 类别基本薪资/奖金/津贴/社保/扣款

    @Column(name = "calculation_method", nullable = false)
    private String calculationMethod; // 计算方法固定值/百分比/公式计算

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

     @Column(name = "params", columnDefinition = "TEXT")
    private String params; // JSON格式的参数字符串


    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "description")
    private String description;

      /**
     * JPA实体保存前的回调方法
     * 自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

     // 如果确实缺少getId()方法，手动添加
    public Long getId() {
        return this.projectId; // 注意：这里返回projectId，因为字段名是projectId
    }
    
    // 或者更好的方式：添加getProjectId()方法（如果前端使用projectId字段）
    public Long getProjectId() {
        return this.projectId;
    }

}

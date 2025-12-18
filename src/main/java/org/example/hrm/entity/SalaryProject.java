package org.example.hrm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 薪酬项目实体类
 * 对应数据库表: project_薪资项目表
 */
@Data
@Entity
@Table(name = "project")
@NoArgsConstructor
@AllArgsConstructor
public class SalaryProject implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 添加这个注解
    @Column(name = "project_id")
    private Long projectId;
    
    /**
     * 项目编码，唯一
     */
    @Column(name = "project_code", nullable = false, length = 50)
    private String projectCode;
    
    /**
     * 项目名称
     */
    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;
    
    /**
     * 项目类型：1=收入项，2=扣减项
     */
    @Column(name = "project_type", nullable = false)
    private Integer projectType;
    
    /**
     * 项目类别：salary/insurance/bonus/deduction 等
     */
    @Column(name = "category", nullable = false, length = 20)
    private String category;
    
    /**
     * 计算方式：fixed/percentage/formula 等
     */
    @Column(name = "calculation_method", nullable = false, length = 20)
    private String calculationMethod;
    
    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    /**
     * 状态：1=启用，0=禁用
     */
    @Column(name = "status")
    private Integer status = 1;
    
    /**
     * 项目描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    /**
     * 计算参数，JSON格式存储
     */
    @Column(name = "params", columnDefinition = "TEXT")
    private String params;
    
    /**
     * JPA持久化前的回调，自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
        // 设置默认值
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        if (this.status == null) {
            this.status = 1; // 默认启用
        }
    }
    
    /**
     * 获取项目类型名称
     */
    @Transient
    public String getProjectTypeName() {
        if (projectType == null) return "";
        switch (projectType) {
            case 1: return "收入项";
            case 2: return "扣减项";
            default: return "未知类型";
        }
    }
    
    /**
     * 获取状态名称
     */
    @Transient
    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case 1: return "启用";
            case 0: return "禁用";
            default: return "未知状态";
        }
    }
    
    /**
     * 判断是否启用
     */
    @Transient
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 判断是否是收入项
     */
    @Transient
    public boolean isIncome() {
        return projectType != null && projectType == 1;
    }
    
    /**
     * 判断是否是扣减项
     */
    @Transient
    public boolean isDeduction() {
        return projectType != null && projectType == 2;
    }

     // 重写 equals 和 hashCode 方法，避免序列化问题
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalaryProject that = (SalaryProject) o;
        return Objects.equals(projectId, that.projectId) &&
               Objects.equals(projectCode, that.projectCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(projectId, projectCode);
    }
}
package org.example.hrm.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "standard_position", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_position_level", 
                           columnNames = {"position_id", "standard_level"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StandardPosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id", nullable = false)
    private Standard standard;
    
    // 标准等级字段 - 对应数据库的 standard_level 列
    @Column(name = "standard_level", length = 50, nullable = false)
    private String standardLevel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 创建时间自动设置
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    // 验证标准等级是否有效
    public boolean isValidLevel() {
        if (standardLevel == null) return false;
        return standardLevel.equals("初级") || 
               standardLevel.equals("中级") || 
               standardLevel.equals("高级");
    }
    
    // 获取完整显示名称
    public String getFullDisplayName() {
        String level = (standardLevel != null && !standardLevel.isEmpty()) ? 
                      " - " + standardLevel : "";
        return getStandard().getStandardName() + level;
    }
}
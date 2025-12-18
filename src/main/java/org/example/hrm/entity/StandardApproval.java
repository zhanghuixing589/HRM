package org.example.hrm.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "standard_approval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "standard")  // 防止toString时的无限循环
public class StandardApproval {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    // 关联标准 - 与 StandardPosition 保持一致的映射方式
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id")
    private Standard standard;
    
    @Column(name = "submitter_id")
    private Long submitterId;
    
    @Column(name = "submitter_name")
    private String submitterName;
    
    @Column(name = "approver_id")
    private Long approverId;

    // 新增字段：审核人姓名
    @Column(name = "approver_name")
    private String approverName;

    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;
    
    @Column(name = "approval_status") // 2-待审核 3-通过 0-驳回
    private Integer approvalStatus;
    
    @Column(name = "approval_opinion", columnDefinition = "text")
    private String approvalOpinion;
    
    @Column(name = "is_active", columnDefinition = "tinyint default 1")
    private Integer isActive; // 1-当前有效 0-历史记录
    
    // 便捷方法：获取标准ID
    public Long getStandardId() {
        return standard != null ? standard.getStandardId() : null;
    }
    
    // 便捷方法：设置标准ID
    public void setStandardId(Long standardId) {
        if (this.standard == null) {
            this.standard = new Standard();
        }
        this.standard.setStandardId(standardId);
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.submitTime == null) {
            this.submitTime = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = 1;
        }
    }
    
    // 重写 equals 和 hashCode（与 StandardPosition 保持一致）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardApproval that = (StandardApproval) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
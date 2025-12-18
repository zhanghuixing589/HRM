package org.example.hrm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;

@Entity
@Table(name = "standard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Standard {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standard_id")
    private Long standardId;
    
    @Column(name = "standard_code", unique = true, nullable = false)
    private String standardCode;
    
    @Column(name = "standard_name")
    private String standardName;
    
    @Column(name = "creator_id")
    private Long creatorId;
    
    @Column(name = "registrar_id")
    private Long registrarId;
    
    @Column(name = "registration_time")
    private LocalDateTime registrationTime;
    
    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status; // 1-草稿 2-待审核 3-已生效 0-驳回
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    
    // 关联明细 - 使用@JsonManagedReference
    @OneToMany(mappedBy = "standard", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("standard-items")
    @OrderBy("id ASC")
    private List<StandardItem> items = new ArrayList<>();
    
    // 关联审核记录 - 使用@JsonManagedReference
    @OneToMany(mappedBy = "standard", cascade = CascadeType.ALL)
    @JsonManagedReference("standard-approvals")
    @OrderBy("submitTime DESC")  // 按提交时间倒序排列
    private List<StandardApproval> approvals = new ArrayList<>();
    
    // 关联职位 - 使用@JsonManagedReference
    @OneToMany(mappedBy = "standard", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("standard-positions")
    private List<StandardPosition> standardPositions = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = 1; // 默认草稿状态
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

     // 便捷方法：添加审核记录
    public void addApproval(StandardApproval approval) {
        if (approvals == null) {
            approvals = new ArrayList<>();
        }
        approval.setStandard(this);
        approvals.add(approval);
    }

     // 便捷方法：获取当前有效的审核记录
    public StandardApproval getActiveApproval() {
        if (approvals == null || approvals.isEmpty()) {
            return null;
        }
        return approvals.stream()
                .filter(a -> a.getIsActive() != null && a.getIsActive() == 1)
                .findFirst()
                .orElse(null);
    }

}

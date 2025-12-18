package org.example.hrm.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "standard_approval_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardApprovalLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    @Column(name = "approval_id")
    private Long approvalId;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operation_type", length = 20)
    private String operationType; // Submit-提交，approval-审核
    
     @Column(name = "operator_name", length = 100)
    private String operatorName; // 添加操作人姓名字段
    
    @Column(name = "operation_content", columnDefinition = "text")
    private String operationContent;
    
    @Column(name = "operation_time")
    private LocalDateTime operationTime;
    
    @PrePersist
    protected void onCreate() {
        this.operationTime = LocalDateTime.now();
    }
}
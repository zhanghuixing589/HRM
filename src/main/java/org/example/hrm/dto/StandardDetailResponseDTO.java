package org.example.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class StandardDetailResponseDTO {
    private Long standardId;
    private String standardCode;
    private String standardName;
    private Long creatorId;
    private String creatorName;
    private Long registrarId;
    private String registrarName;
    private Integer status;
    private LocalDateTime registrationTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 明细项目列表
    private List<StandardItemInfoDTO> items;
    
    // 关联职位列表
    private List<PositionInfoDTO> positions;

     // 审核记录列表
    private List<ApprovalInfoDTO> approvals;

    // 当前活跃的审核记录（用于审核页面）
    private ApprovalInfoDTO currentApproval;
    
    // 审核日志列表（详细操作记录）
    private List<ApprovalLogDTO> approvalLogs;
    
    @Data
    public static class StandardItemInfoDTO {
        private Long id;
        private String projectCode;
        private String projectName;
        private Integer projectType;
        private String category;
        private String calculationMethod;
        private BigDecimal amount;
        private BigDecimal unitPrice;
        private BigDecimal ratio;
        private Integer sortOrder;
    }
    
    @Data
    public static class PositionInfoDTO {
        private Long posId;
        private String posCode;
        private String posName;
        private Long orgId;
        private String orgName;
        private Integer status;
    }


       // 审核记录DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalInfoDTO {
        private Long id;
        private Long standardId;
        private Long submitterId;
        private String submitterName;
        private Long approverId;
        private String approverName;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime submitTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approvalTime;
        
        private Integer approvalStatus; // 2-待审核 3-通过 0-驳回
        private String approvalOpinion;
        private Integer isActive; // 1-有效 0-无效
    }
    
    // 审核日志DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalLogDTO {
        private Long id;
        private Long approvalId;
        private Long operatorId;
        private String operatorName;
        private String operationType; // Submit, Approve, Reject, Recall
        private String operationContent;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operationTime;
    }

    // 审核历史记录DTO（包含操作日志）
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public static class ApprovalHistoryDTO {
    private Long id;
    private Long standardId;
    private Long submitterId;
    private String submitterName;
    private Long approverId;
    private String approverName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;
    
    private Integer approvalStatus;
    private String approvalOpinion;
    private Integer isActive;
    
    // 该审核记录的操作日志
    private List<ApprovalLogDTO> logs;
}
}
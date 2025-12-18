package org.example.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalProjectionDTO {
    private Long id;
    private Long standardId;
    private Long submitterId;
    private Long approverId;
    private LocalDateTime submitTime;
    private LocalDateTime approvalTime;
    private Integer approvalStatus;
    private String approvalOpinion;
    private Integer isActive;
}
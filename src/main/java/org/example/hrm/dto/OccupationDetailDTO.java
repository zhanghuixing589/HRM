package org.example.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupationDetailDTO {
    private Long standardId;
    private String standardCode;
    private String standardName;
    private Integer status;
    private Long creatorId;
    private LocalDateTime createdAt;
}
package org.example.hrm.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StandardResponseDTO {
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
    
    // 关联职位信息
    private List<PositionInfoDTO> positions;
    
    // 项目统计信息
    private Integer projectCount;
    private BigDecimal totalAmount;
    
    @Data
    public static class PositionInfoDTO {
        private Long posId;
        private String posCode;
        private String posName;
        private Long orgId;
        private String orgName;
    }
}
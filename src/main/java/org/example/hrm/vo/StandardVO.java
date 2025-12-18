package org.example.hrm.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class StandardVO {
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
    private List<StandardItemVO> items;
    private List<PositionVO> positions; // 实时职位信息
}

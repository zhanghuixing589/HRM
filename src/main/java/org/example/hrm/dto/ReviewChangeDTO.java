package org.example.hrm.dto;

import lombok.Data;
import java.util.Map;
import javax.persistence.Transient;

@Data
public class ReviewChangeDTO {
  private Long changeId;
  private Integer action; // 2:通过, 3:驳回
  private String reviewComment; // 审核意见
  private Map<String, Object> changedData; // 经理修改的字段

  // 仅用于前端展示，不存储
  @Transient
  private Map<String, Object> originalData; // 原始数据
}
package org.example.hrm.dto;

import lombok.Data;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ReviewDTO {
  private Long arcId;
  private Integer action; // 2:通过, 3:驳回

  @JsonProperty("comment")
  private String comment; // 审核意见/驳回原因
  private Map<String, Object> changedData; // 经理修改的字段
}

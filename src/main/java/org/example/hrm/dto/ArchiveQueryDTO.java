package org.example.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArchiveQueryDTO {
  private String name;
  private String idCard;
  private Integer status;
  private Long writeId;
  private Long reviewId;
  private Long thirdOrgId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTimeStart;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTimeEnd;

  private Integer page = 0;
  private Integer size = 10;
}

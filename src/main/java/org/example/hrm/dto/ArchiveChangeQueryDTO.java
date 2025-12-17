package org.example.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArchiveChangeQueryDTO {
  private String archiveName;
  private String archiveCode;
  private Integer status;
  private Long applyId;
  private Long reviewId;
  private String changeReason;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTimeStart;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTimeEnd;

  private Integer page = 0;
  private Integer size = 10;
}
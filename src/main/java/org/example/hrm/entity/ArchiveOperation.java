package org.example.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import java.time.LocalDateTime;

import lombok.Data;

@Entity
@Table(name = "archive_operation")
@Data
public class ArchiveOperation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reviewId") // 映射到数据库的reviewId字段
  private Long reviewId;

  @Column(name = "arcId", nullable = false)
  private Long arcId;

  @Column(name = "operationType", nullable = false)
  private Integer operationType; // 1:提交, 2:审核通过, 3:审核驳回, 4:重新提交

  @Column(name = "operatorId", nullable = false)
  private Long operatorId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "operationTime", nullable = false)
  private LocalDateTime operationTime = LocalDateTime.now();

  @Column(name = "operationComment", length = 100)
  private String operationComment;

  @Column(name = "changeFields", columnDefinition = "json")
  private String changeFields; // JSON格式，记录修改的字段

  // 非数据库字段
  @Transient
  private String operatorName;

  @Transient
  private String operationTypeName;
}
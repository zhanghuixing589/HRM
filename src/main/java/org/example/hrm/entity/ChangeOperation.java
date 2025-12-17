package org.example.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "change_operation")
@Data
public class ChangeOperation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reviewId")
  private Long reviewId;

  @Column(name = "changeId", nullable = false)
  private Long changeId;

  @Column(name = "operationType", nullable = false)
  private Integer operationType; // 1:提交, 2:审核通过, 3:审核驳回, 4:重新提交

  @Column(name = "operatorId", nullable = false)
  private Long operatorId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "operationTime", nullable = false)
  private LocalDateTime operationTime = LocalDateTime.now();

  @Column(name = "operationComment", length = 100)
  private String operationComment;

  // 非数据库字段
  @Transient
  private String operatorName;

  @Transient
  private String operationTypeName;
}
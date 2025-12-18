package org.example.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name = "archive_change")
@Data
public class ArchiveChange {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "changeId")
  private Long changeId;

  @Column(name = "changeCode", unique = true, nullable = false, length = 10)
  private String changeCode;

  @Column(name = "arcId", nullable = false)
  private Long arcId;

  @Column(name = "changeReason", nullable = false, length = 100)
  private String changeReason;

  @Column(name = "beforeData", columnDefinition = "json")
  private String beforeData;

  @Column(name = "afterData", columnDefinition = "json")
  private String afterData;

  @Column(name = "changedFields", columnDefinition = "json")
  private String changedFields;

  @Column(name = "applyId", nullable = false)
  private Long applyId;

  @Column(name = "status", nullable = false)
  private Integer status = 1; // 1:待复核, 2:已通过, 3:已驳回

  @Column(name = "reviewId")
  private Long reviewId;

  @Column(name = "reviewComment", length = 100)
  private String reviewComment;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "createTime", nullable = false)
  private LocalDateTime createTime = LocalDateTime.now();

  // 非数据库字段（用于显示）
  @Transient
  private String archiveName;

  @Transient
  private String archiveCode;

  @Transient
  private String applicantName;

  @Transient
  private String reviewerName;

  @Transient
  private String statusName;

  // 关联档案信息
  @Transient
  private Archive archive;

  // 添加解析后的数据（不存储到数据库）
  @Transient
  private Map<String, Object> beforeDataParsed;

  @Transient
  private Map<String, Object> afterDataParsed;

  @Transient
  private List<String> changedFieldsParsed;

  // 添加方法解析JSON数据
  public Map<String, Object> getBeforeDataParsed() {
    if (beforeDataParsed == null && beforeData != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        beforeDataParsed = mapper.readValue(beforeData, Map.class);
      } catch (Exception e) {
        // 解析失败，返回空Map
        beforeDataParsed = new HashMap<>();
      }
    }
    return beforeDataParsed;
  }

  public Map<String, Object> getAfterDataParsed() {
    if (afterDataParsed == null && afterData != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        afterDataParsed = mapper.readValue(afterData, Map.class);
      } catch (Exception e) {
        // 解析失败，返回空Map
        afterDataParsed = new HashMap<>();
      }
    }
    return afterDataParsed;
  }

  public List<String> getChangedFieldsParsed() {
    if (changedFieldsParsed == null && changedFields != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        changedFieldsParsed = mapper.readValue(changedFields, List.class);
      } catch (Exception e) {
        // 解析失败，返回空列表
        changedFieldsParsed = new ArrayList<>();
      }
    }
    return changedFieldsParsed;
  }
}
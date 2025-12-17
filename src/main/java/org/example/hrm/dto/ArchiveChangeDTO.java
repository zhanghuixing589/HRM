package org.example.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.util.Map;
import javax.persistence.Transient;

@Data
public class ArchiveChangeDTO {
  private Long arcId;
  private String changeReason;

  // 变更后的数据
  private String name;
  private Integer sex;
  private String idCard;
  private String title; // 职称
  private Long salaryStandard;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate birDate;

  private String nationality;
  private String qualification; // 学历
  private String email;
  private String telephone;
  private String phone;
  private String address;
  private String zipCode;
  private String country;
  private String birAddress;
  private String belief; // 宗教信仰
  private String identity; // 政治面貌
  private String major; // 学历专业
  private String photoPath;

  // 仅用于前端展示，不存储
  @Transient
  private Map<String, Object> originalData; // 原始数据
}
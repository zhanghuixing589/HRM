package org.example.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Entity
@Table(name = "archive")
@Data
public class Archive {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "arcId")
  private Long arcId;

  @Column(name = "arcCode", unique = true, nullable = false, length = 12)
  private String arcCode;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "sex", nullable = false)
  private Integer sex; // 1:男, 2:女

  @Column(name = "IDcard", nullable = false, length = 18)
  private String idCard;

  @Column(name = "firstOrgId", nullable = false)
  private Long firstOrgId;

  @Column(name = "secondOrgId", nullable = false)
  private Long secondOrgId;

  @Column(name = "thirdOrgId", nullable = false)
  private Long thirdOrgId;

  @Column(name = "positionName", nullable = false, length = 100)
  private String positionName;

  @Column(name = "title", nullable = false, length = 20)
  private String title; // 初级、中级、高级

  @Column(name = "salaryStandard", nullable = false)
  private Long salaryStandard;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @Column(name = "birDate", nullable = false)
  private LocalDate birDate;

  @Column(name = "nationality", nullable = false, length = 50)
  private String nationality;

  @Column(name = "qualification", nullable = false, length = 50)
  private String qualification; // 学历

  @Column(name = "email", length = 100)
  private String email;

  @Column(name = "telephone", length = 11)
  private String telephone;

  @Column(name = "phone", length = 11)
  private String phone;

  @Column(name = "address", length = 100)
  private String address;

  @Column(name = "zipCode", length = 20)
  private String zipCode;

  @Column(name = "country", length = 50)
  private String country;

  @Column(name = "birAddress", length = 100)
  private String birAddress;

  @Column(name = "belief", length = 20)
  private String belief; // 宗教信仰

  @Column(name = "identity", length = 50)
  private String identity; // 政治面貌

  @Column(name = "major", length = 50)
  private String major; // 学历专业

  @Column(name = "photoPath", length = 200)
  private String photoPath;

  @Column(name = "writeId", nullable = false)
  private Long writeId; // 登记人ID

  @Column(name = "status", nullable = false)
  private Integer status = 1; // 1:待复核, 2:已通过, 3:已驳回, 4:重新提交待审核, 0:已删除

  @Column(name = "reviewId")
  private Long reviewId; // 复核人ID

  @Column(name = "reason", length = 100)
  private String reason; // 驳回原因

  @Column(name = "resubmitCount")
  private Integer resubmitCount = 0;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "createTime", nullable = false)
  private LocalDateTime createTime = LocalDateTime.now();

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "submitTime")
  private LocalDateTime submitTime;

  // 非数据库字段（用于显示）
  @Transient
  private String firstOrgName;

  @Transient
  private String secondOrgName;

  @Transient
  private String thirdOrgName;

  @Transient
  private String writerName;

  @Transient
  private String reviewerName;

  @Transient
  private String salaryStandardName;

}

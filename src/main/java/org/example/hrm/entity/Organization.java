package org.example.hrm.entity;

import lombok.Data;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization")
@Data
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "orgId") // 映射到驼峰命名的列
  private Long orgId; // 使用Long类型，与Repository的Long类型匹配

  @Column(name = "orgCode", nullable = false, length = 4)
  private String orgCode;

  @Column(name = "orgName", nullable = false, length = 100)
  private String orgName;

  @Column(name = "orgLevel")
  private Integer orgLevel; // 1-一级，2-二级，3-三级

  @Column(name = "parId")
  private Long parId; // 上级机构id

  @Column(name = "status")
  private Integer status = 1; // 1-启用，2-禁用

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "createTime")
  private LocalDateTime createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "updateTime")
  private LocalDateTime updateTime;

  // 用于构建机构树的子节点（非数据库字段）
  @Transient
  private List<Organization> children = new ArrayList<>();

  // 用于显示的父机构名称（非数据库字段）
  @Transient
  private String parentOrgName;
}

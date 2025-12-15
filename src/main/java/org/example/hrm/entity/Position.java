package org.example.hrm.entity;

import lombok.Data;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "position")
@Data
public class Position {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "posId")
  private Long posId;

  @Column(name = "posCode", unique = true, nullable = false, length = 10)
  private String posCode; // 职位编码

  @Column(name = "posName", nullable = false, length = 100)
  private String posName; // 职位名称

  @Column(name = "orgId", nullable = false)
  private Long orgId; // 所属三级机构ID

  @Column(name = "status")
  private Integer status = 1; // 1-启用，0-禁用

  @Column(name = "createTime")
  private LocalDateTime createTime;
  
  @Transient 
  private String orgName; // 关联的机构名称，非数据库字段
}

package org.example.hrm.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "standard")
@Data
public class ArchiveStandard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "standard_id")
  private Long standardId;

  @Column(name = "standard_code", nullable = false, unique = true, length = 50)
  private String standardCode;

  @Column(name = "standard_name", length = 200)
  private String standardName;

  @Column(name = "creator_id")
  private Long creatorId;

  @Column(name = "registrar_id")
  private Long registrarId;

  @Column(name = "registration_time")
  private LocalDateTime registrationTime;

  @Column(name = "status")
  private Integer status = 1; // 1-草稿, 2-待审核, 3-已生效, 0-驳回

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();
}
package org.example.hrm.entity;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "standard_position")
@Data
public class ArchiveStandardPosition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "standard_id")
  private Long standardId;

  @Column(name = "position_id")
  private Long positionId;
}
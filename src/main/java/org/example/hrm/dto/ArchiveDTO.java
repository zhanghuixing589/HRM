package org.example.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ArchiveDTO {
  private String name;
  private Integer sex;
  private String idCard;
  private Long firstOrgId;
  private Long secondOrgId;
  private Long thirdOrgId;
  private String positionName;
  private String title;
  private Long salaryStandard;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate birDate;

  private String nationality;
  private String qualification;
  private String email;
  private String telephone;
  private String phone;
  private String address;
  private String zipCode;
  private String country;
  private String birAddress;
  private String belief;
  private String identity;
  private String major;
  private String photoPath;
}

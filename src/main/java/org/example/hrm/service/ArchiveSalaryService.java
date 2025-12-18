// ArchiveSalaryService.java - 新服务类，避免冲突
package org.example.hrm.service;

import org.example.hrm.entity.ArchiveStandard;
import org.example.hrm.repository.ArchiveStandardRepository;
import org.example.hrm.repository.ArchiveStandardPositionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArchiveSalaryService {

  @Autowired
  private ArchiveStandardRepository archiveStandardRepository;

  @Autowired
  private ArchiveStandardPositionRepository archiveStandardPositionRepository;

  // 根据职位ID和职称筛选薪酬标准
  public List<ArchiveStandard> getStandardsByPositionAndTitle(Long positionId, String title) {
    log.info("根据职位ID: {} 和职称: {} 筛选薪酬标准", positionId, title);

    if (positionId == null) {
      return new ArrayList<>();
    }

    // 先获取该职位关联的所有薪酬标准
    List<ArchiveStandard> standardsByPosition = archiveStandardRepository.findByPositionId(positionId);

    if (standardsByPosition.isEmpty()) {
      log.warn("职位ID: {} 没有关联的薪酬标准", positionId);
      return new ArrayList<>();
    }

    if (!StringUtils.hasText(title)) {
      // 如果没有职称，返回所有关联的标准
      return standardsByPosition;
    }

    // 根据职称进行模糊匹配
    return standardsByPosition.stream()
        .filter(standard -> {
          if (standard.getStandardName() == null) {
            return false;
          }
          // 将标准名称转为小写，便于模糊匹配
          String standardName = standard.getStandardName().toLowerCase();
          String titleLower = title.toLowerCase();

          // 检查标准名称是否包含职称
          return standardName.contains(titleLower);
        })
        .collect(Collectors.toList());
  }

  // 根据关键字搜索薪酬标准（不限定职位）
  public List<ArchiveStandard> searchStandards(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return archiveStandardRepository.findActiveStandards();
    }
    return archiveStandardRepository.findByKeyword(keyword);
  }

  // 获取所有已生效的薪酬标准
  public List<ArchiveStandard> getAllActiveStandards() {
    return archiveStandardRepository.findActiveStandards();
  }

  // 根据职位ID获取所有关联的薪酬标准（用于前端展示）
  public List<Map<String, Object>> getStandardsForPosition(Long positionId) {
    List<ArchiveStandard> standards = archiveStandardRepository.findByPositionId(positionId);

    return standards.stream()
        .map(standard -> {
          Map<String, Object> map = new HashMap<>();
          map.put("standardId", standard.getStandardId());
          map.put("standardCode", standard.getStandardCode());
          map.put("standardName", standard.getStandardName());
          map.put("status", standard.getStatus());
          return map;
        })
        .collect(Collectors.toList());
  }
}
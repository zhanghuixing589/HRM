package org.example.hrm.service;

import org.example.hrm.entity.Organization;
import org.example.hrm.entity.Position;
import org.example.hrm.repository.OrganizationRepository;
import org.example.hrm.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class PositionService {

  @Autowired
  private PositionRepository positionRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  // 获取所有职位（带机构名称）
  public List<Position> getAllPositions() {
    List<Position> positions = positionRepository.findAll();

    // 批量获取机构信息
    Set<Long> orgIds = positions.stream()
        .map(Position::getOrgId)
        .collect(Collectors.toSet());

    // 批量查询机构
    Map<Long, Organization> orgMap = organizationRepository.findAllById(orgIds)
        .stream()
        .collect(Collectors.toMap(Organization::getOrgId, org -> org));

    // 补充机构名称
    return positions.stream()
        .map(position -> {
          Organization org = orgMap.get(position.getOrgId());
          if (org != null) {
            position.setOrgName(org.getOrgName());
          }
          return position;
        })
        .collect(Collectors.toList());
  }

  // 根据机构id获取职位
  public List<Position> getPositionsByOrgId(Long orgId) {
    List<Position> positions = positionRepository.findByOrgId(orgId);

    // 补充机构名称
    Optional<Organization> orgOpt = organizationRepository.findById(orgId);
    String orgName = orgOpt.isPresent() ? orgOpt.get().getOrgName() : "";

    return positions.stream()
        .peek(position -> position.setOrgName(orgName))
        .collect(Collectors.toList());
  }

  // 根据状态获取职位
  public List<Position> getPositionsByStatus(Integer status) {
    List<Position> positions = positionRepository.findByStatus(status);

    // 批量获取机构信息
    Set<Long> orgIds = positions.stream()
        .map(Position::getOrgId)
        .collect(Collectors.toSet());

    // 批量查询机构
    Map<Long, Organization> orgMap = organizationRepository.findAllById(orgIds)
        .stream()
        .collect(Collectors.toMap(Organization::getOrgId, org -> org));

    // 补充机构名称
    return positions.stream()
        .map(position -> {
          Organization org = orgMap.get(position.getOrgId());
          if (org != null) {
            position.setOrgName(org.getOrgName());
          }
          return position;
        })
        .collect(Collectors.toList());
  }

  // 创建或更新职位
  public Position savePosition(Position position) {
    validatePosition(position);

    // 如果是更新操作，先获取原职位
    Position existingPosition = null;
    if (position.getPosId() != null) {
      existingPosition = positionRepository.findById(position.getPosId())
          .orElseThrow(() -> new RuntimeException("职位不存在"));

      // 对于更新操作，确保createTime不为空
      if (existingPosition.getCreateTime() != null) {
        position.setCreateTime(existingPosition.getCreateTime());
      }

      // 确保orgId不变（编辑时不能修改所属机构）
      position.setOrgId(existingPosition.getOrgId());
    }

    // 检查同一三级机构下不能有同名职位
    List<Position> sameNamePositions = positionRepository
        .findByOrgIdAndPosName(position.getOrgId(), position.getPosName());

    // 如果是更新操作，需要排除自己
    if (position.getPosId() != null) {
      sameNamePositions = sameNamePositions.stream()
          .filter(p -> !p.getPosId().equals(position.getPosId()))
          .collect(Collectors.toList());
    }

    if (!sameNamePositions.isEmpty()) {
      throw new RuntimeException("同一机构下不能有重复的职位名称");
    }

    // 验证所属机构必须是三级机构
    Optional<Organization> orgOpt = organizationRepository
        .findThirdLevelOrgById(position.getOrgId());
    if (!orgOpt.isPresent()) {
      throw new RuntimeException("职位必须属于三级机构");
    }

    if (position.getPosId() == null) {
      // 新增
      position.setCreateTime(LocalDateTime.now());
    }

    // 保存前进行日志记录，便于调试
    log.info("保存职位信息: posId={}, posCode={}, posName={}, orgId={}, status={}",
        position.getPosId(), position.getPosCode(), position.getPosName(),
        position.getOrgId(), position.getStatus());

    try {
      return positionRepository.save(position);
    } catch (Exception e) {
      log.error("保存职位失败: {}", e.getMessage(), e);
      if (e.getMessage().contains("constraint") || e.getMessage().contains("ConstraintViolation")) {
        throw new RuntimeException("保存失败：可能存在重复的职位编码或数据冲突");
      }
      throw new RuntimeException("保存职位失败: " + e.getMessage());
    }
  }

  // 删除职位
  public void deletePosition(Long posId) {
    // 检查是否有员工使用该职位
    // 这里假设有EmployeeService可以查询职位使用情况
    /*
     * boolean isUsed = employeeService.isPositionInUse(posId);
     * if (isUsed) {
     * throw new RuntimeException("该职位正在被使用，无法删除");
     * } else {
     */
    positionRepository.deleteById(posId);
    log.info("删除职位: {}", posId);
  }

  // 职位数据校验
  private void validatePosition(Position position) {
    if (!StringUtils.hasText(position.getPosCode())) {
      throw new RuntimeException("职位编码不能为空");
    }

    if (!StringUtils.hasText(position.getPosName())) {
      throw new RuntimeException("职位名称不能为空");
    }

    if (position.getOrgId() == null) {
      throw new RuntimeException("所属机构不能为空");
    }

    // 检查职位编码唯一性（排除当前职位）
    Optional<Position> existing = positionRepository.findByPosCode(position.getPosCode());
    if (existing.isPresent()) {
      // 如果是新增操作，直接报错
      if (position.getPosId() == null) {
        throw new RuntimeException("职位编码已存在");
      }
      // 如果是更新操作，检查是不是同一个职位
      else if (!existing.get().getPosId().equals(position.getPosId())) {
        throw new RuntimeException("职位编码已存在");
      }
    }
  }

  // 启用或禁用职位
  public Position toggleStatus(Long posId, Integer status) {
    Position position = positionRepository.findById(posId)
        .orElseThrow(() -> new RuntimeException("职位不存在"));

    position.setStatus(status);
    return positionRepository.save(position);
  }

  // 根据机构id列表批量查询职位
  public List<Position> getPositionsByOrgIds(List<Long> orgIds) {
    List<Position> positions = positionRepository.findByOrgIds(orgIds);

    // 获取机构名称
    Map<Long, String> orgNameMap = organizationRepository.findAllById(orgIds)
        .stream()
        .collect(Collectors.toMap(Organization::getOrgId, Organization::getOrgName));

    // 补充机构名称
    return positions.stream()
        .peek(position -> position.setOrgName(orgNameMap.get(position.getOrgId())))
        .collect(Collectors.toList());
  }

  // 根据id获取职位（带机构名称）
  public Position getPositionById(Long posId) {
    Position position = positionRepository.findById(posId)
        .orElseThrow(() -> new RuntimeException("职位不存在"));

    // 补充机构名称
    Optional<Organization> orgOpt = organizationRepository.findById(position.getOrgId());
    if (orgOpt.isPresent()) {
      position.setOrgName(orgOpt.get().getOrgName());
    }

    return position;
  }

  // 统计某机构下的职位数量
  public long countByOrgId(Long orgId) {
    return positionRepository.countByOrgId(orgId);
  }

  // 获取所有启用的职位
  public List<Position> getActivePositions() {
    return getPositionsByStatus(1);
  }

  // 根据职位编码获取职位
  public Position getPositionByCode(String posCode) {
    return positionRepository.findByPosCode(posCode)
        .orElseThrow(() -> new RuntimeException("职位不存在"));
  }

  // 分页获取所有职位
  public Page<Position> getAllPositionsWithPage(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
    Page<Position> positionPage = positionRepository.findAll(pageable);

    // 批量获取机构信息
    Set<Long> orgIds = positionPage.getContent().stream()
        .map(Position::getOrgId)
        .collect(Collectors.toSet());

    Map<Long, Organization> orgMap = organizationRepository.findAllById(orgIds)
        .stream()
        .collect(Collectors.toMap(Organization::getOrgId, org -> org));

    // 补充机构名称
    positionPage.getContent().forEach(position -> {
      Organization org = orgMap.get(position.getOrgId());
      if (org != null) {
        position.setOrgName(org.getOrgName());
      }
    });

    return positionPage;
  }

  // 根据机构id分页获取职位
  public Page<Position> getPositionsByOrgIdWithPage(Long orgId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
    Page<Position> positionPage = positionRepository.findByOrgId(orgId, pageable);

    // 补充机构名称
    if (!positionPage.isEmpty()) {
      Optional<Organization> orgOpt = organizationRepository.findById(orgId);
      String orgName = orgOpt.isPresent() ? orgOpt.get().getOrgName() : "";
      positionPage.getContent().forEach(position -> position.setOrgName(orgName));
    }

    return positionPage;
  }

  // 根据状态分页获取职位
  public Page<Position> getPositionsByStatusWithPage(Integer status, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
    Page<Position> positionPage = positionRepository.findByStatus(status, pageable);

    // 批量获取机构信息
    Set<Long> orgIds = positionPage.getContent().stream()
        .map(Position::getOrgId)
        .collect(Collectors.toSet());

    Map<Long, Organization> orgMap = organizationRepository.findAllById(orgIds)
        .stream()
        .collect(Collectors.toMap(Organization::getOrgId, org -> org));

    // 补充机构名称
    positionPage.getContent().forEach(position -> {
      Organization org = orgMap.get(position.getOrgId());
      if (org != null) {
        position.setOrgName(org.getOrgName());
      }
    });

    return positionPage;
  }
}
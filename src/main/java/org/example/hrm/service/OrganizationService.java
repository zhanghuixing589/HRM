package org.example.hrm.service;

import org.example.hrm.entity.Organization;
import org.example.hrm.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
public class OrganizationService {

  @Autowired
  private OrganizationRepository organizationRepository;

  // 生成机构编码
  private String generateOrgCode(Integer orgLevel) {
    if (orgLevel == null || orgLevel < 1 || orgLevel > 3) {
      throw new RuntimeException("机构级别必须为1、2或3级");
    }
    
    // 根据级别确定前缀
    int prefix = orgLevel;
    
    // 查询该级别下最大的机构编码
    String maxOrgCode = organizationRepository.findMaxOrgCodeByLevel(orgLevel);
    
    int nextNumber = 1; // 默认从001开始
    
    if (maxOrgCode != null && maxOrgCode.length() == 4) {
      try {
        // 提取后三位数字
        String numberPart = maxOrgCode.substring(1);
        int currentNumber = Integer.parseInt(numberPart);
        nextNumber = currentNumber + 1;
        
        // 确保不超过999
        if (nextNumber > 999) {
          throw new RuntimeException("该级别机构数量已超过999个限制");
        }
      } catch (NumberFormatException e) {
        log.warn("解析机构编码失败: {}", maxOrgCode);
        nextNumber = 1;
      }
    }
    
    // 生成4位编码，格式：级别(1位) + 序号(3位，补零)
    return String.format("%d%03d", prefix, nextNumber);
  }

  /**
   * 获取机构树
   */
  public List<Organization> getOrganizationTree() {
    // 获取所有机构
    List<Organization> allOrgs = organizationRepository.findAll();

    log.info("从数据库获取到 {} 个机构", allOrgs.size());

    // 如果没有数据或数据异常，直接返回
    if (allOrgs.isEmpty()) {
      return new ArrayList<>();
    }

    // 构建机构树
    Map<Long, Organization> orgMap = new HashMap<>();
    List<Organization> rootOrgs = new ArrayList<>();

    // 第一遍，放入Map
    for (Organization org : allOrgs) {
      // 跳过无效数据
      if (org.getOrgId() == null || org.getOrgId() == 0) {
        continue;
      }
      
      // 确保有createTime
      if (org.getCreateTime() == null) {
        org.setCreateTime(LocalDateTime.now());
      }
      
      // 初始化子节点列表
      if (org.getChildren() == null) {
        org.setChildren(new ArrayList<>());
      }
      
      orgMap.put(org.getOrgId(), org);
    }

    // 第二遍，构建树结构
    for (Organization org : allOrgs) {
      // 跳过无效数据
      if (org.getOrgId() == null || org.getOrgId() == 0) {
        continue;
      }

      Long parId = org.getParId();
      if (parId == null || parId == 0) {
        // 根节点（没有父级ID）
        rootOrgs.add(org);
      } else {
        // 子节点，加入父节点的子节点列表
        Organization parent = orgMap.get(parId);
        if (parent != null) {
          if (parent.getChildren() == null) {
            parent.setChildren(new ArrayList<>());
          }
          parent.getChildren().add(org);
        } else {
          log.warn("找不到父级机构 ID: {}, 子机构: {} (ID: {})", 
              parId, org.getOrgName(), org.getOrgId());
        }
      }
    }

    // 按orgLevel和机构编码排序
    Comparator<Organization> comparator = Comparator
        .comparing(Organization::getOrgLevel, Comparator.nullsFirst(Integer::compareTo))
        .thenComparing(Organization::getOrgCode, Comparator.nullsFirst(String::compareTo));

    // 对每个机构的子节点排序
    for (Organization org : orgMap.values()) {
      if (org.getChildren() != null && !org.getChildren().isEmpty()) {
        org.getChildren().sort(comparator);
      }
    }

    // 最后对根节点排序
    rootOrgs.sort(comparator);

    return rootOrgs;
  }

  /**
   * 根据级别获取机构列表
   */
  public List<Organization> getOrgsByLevel(Integer orgLevel) {
    if (orgLevel == null) {
      return Collections.emptyList();
    }
    return organizationRepository.findByOrgLevel(orgLevel);
  }

  /**
   * 根据父级id获取子机构列表
   */
  public List<Organization> getOrgsByParentId(Long parId) {
    if (parId == null) {
      return Collections.emptyList();
    }
    return organizationRepository.findByParId(parId);
  }

  /**
   * 获取所有启用的机构
   */
  public List<Organization> getActiveOrgs() {
    return organizationRepository.findByStatus(1);
  }

  /**
   * 获取所有三级机构
   */
  public List<Organization> getAllThirdLevelOrgs() {
    return organizationRepository.findAllThirdLevelOrgs();
  }

  /**
   * 创建或更新机构
   */
  public Organization saveOrganization(Organization org) {
    log.info("收到机构保存请求: orgId={}, orgName={}, orgLevel={}, parId={}",
        org.getOrgId(), org.getOrgName(), org.getOrgLevel(), org.getParId());

    // 处理前端传递的 "null" 字符串
    if (org.getParId() != null && org.getParId() == 0) {
      org.setParId(null);
    }

    validateOrganization(org);

    // 如果是更新操作，先获取原机构
    Organization existingOrg = null;
    if (org.getOrgId() != null) {
      existingOrg = organizationRepository.findById(org.getOrgId())
          .orElseThrow(() -> new RuntimeException("机构不存在"));

      // 编辑时不能修改机构级别
      if (!org.getOrgLevel().equals(existingOrg.getOrgLevel())) {
        throw new RuntimeException("编辑机构时不能修改机构级别");
      }

      // 编辑时不能修改上级机构（除非是特殊需求）
      // 这里我们限制编辑时不能修改上级机构
      if (existingOrg.getParId() != null && !existingOrg.getParId().equals(org.getParId())) {
        throw new RuntimeException("编辑机构时不能修改上级机构");
      }
      if (existingOrg.getParId() == null && org.getParId() != null) {
        throw new RuntimeException("编辑机构时不能修改上级机构");
      }

      // 保留原创建时间
      org.setCreateTime(existingOrg.getCreateTime());

      // 保留原机构编码（编辑时不能修改）
      if (StringUtils.isEmpty(org.getOrgCode()) && existingOrg.getOrgCode() != null) {
        org.setOrgCode(existingOrg.getOrgCode());
      }
    }

    // 检查同一父级下是否有同名机构
    boolean isDuplicate = isNameDuplicate(org.getParId(), org.getOrgName(), org.getOrgId());
    if (isDuplicate) {
      // 获取父级机构名称用于错误提示
      String parentName = "顶级";
      if (org.getParId() != null) {
        Organization parent = organizationRepository.findById(org.getParId()).orElse(null);
        if (parent != null) {
          parentName = parent.getOrgName();
        }
      }
      throw new RuntimeException(parentName + "下已存在名为【" + org.getOrgName() + "】的机构");
    }

    LocalDateTime now = LocalDateTime.now();

    if (org.getOrgId() == null) {
      // 新建机构

      // 1. 生成机构编码
      if (StringUtils.isEmpty(org.getOrgCode())) {
        String generatedCode = generateOrgCode(org.getOrgLevel());
        org.setOrgCode(generatedCode);
        log.info("为新机构生成编码: {}", generatedCode);
      }

      // 2. 设置时间
      org.setCreateTime(now);
      org.setUpdateTime(now);

      // 3. 验证机构级别与父级关系
      if (org.getOrgLevel() > 1) {
        if (org.getParId() == null || org.getParId() == 0) {
          throw new RuntimeException("二、三级机构必须有指定的上级机构");
        }

        Optional<Organization> parentOpt = organizationRepository.findById(org.getParId());
        if (!parentOpt.isPresent()) {
          throw new RuntimeException("上级机构不存在");
        }

        Organization parent = parentOpt.get();
        // 验证级别关系：二级上级为一级，三级上级为二级
        if (org.getOrgLevel() - parent.getOrgLevel() != 1) {
          throw new RuntimeException(String.format(
              "机构级别关系错误：%s (级别%d) 不能作为 %s (级别%d) 的父级",
              parent.getOrgName(), parent.getOrgLevel(),
              org.getOrgName(), org.getOrgLevel()));
        }
      }
    } else {
      // 更新机构
      org.setUpdateTime(now);

      // 检查编码是否符合级别规则
      if (!StringUtils.isEmpty(org.getOrgCode()) && org.getOrgCode().length() == 4) {
        char levelChar = org.getOrgCode().charAt(0);
        int codeLevel = Character.getNumericValue(levelChar);
        if (codeLevel != org.getOrgLevel()) {
          log.warn("机构编码级别与机构级别不一致: 编码={}, 级别={}", org.getOrgCode(), org.getOrgLevel());
        }
      }
    }

    // 确保createTime不为null
    if (org.getCreateTime() == null) {
      org.setCreateTime(now);
    }

    // 保存前日志
    log.info("保存机构信息: orgId={}, orgCode={}, orgName={}, orgLevel={}, parId={}, status={}",
        org.getOrgId(), org.getOrgCode(), org.getOrgName(), org.getOrgLevel(),
        org.getParId(), org.getStatus());

    try {
      return organizationRepository.save(org);
    } catch (Exception e) {
      log.error("保存机构失败: {}", e.getMessage(), e);
      if (e.getMessage().contains("constraint") || e.getMessage().contains("ConstraintViolation")) {
        throw new RuntimeException("保存失败：可能存在重复的机构编码或数据冲突");
      }
      throw new RuntimeException("保存机构失败: " + e.getMessage());
    }
  }

  /**
   * 删除机构及其子机构
   */
  public void deleteOrganization(Long orgId) {
    // 检查机构是否存在
    Organization org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new RuntimeException("机构不存在"));
    
    // 检查是否有子机构
    List<Organization> childOrgs = organizationRepository.findByParId(orgId);
    if (childOrgs != null && !childOrgs.isEmpty()) {
      throw new RuntimeException("该机构下有子机构，无法删除");
    }
    
    organizationRepository.deleteById(orgId);
    log.info("删除机构: ID={}, Name={}, Code={}", org.getOrgId(), org.getOrgName(), org.getOrgCode());
  }

  /**
   * 机构数据校验
   */
  private void validateOrganization(Organization org) {
    if (!StringUtils.hasText(org.getOrgName())) {
      throw new IllegalArgumentException("机构名称不能为空");
    }
    if (org.getOrgLevel() == null || org.getOrgLevel() < 1 || org.getOrgLevel() > 3) {
      throw new RuntimeException("机构级别必须为1、2或3级");
    }
    
    // 如果提供了机构编码，验证其格式
    if (!StringUtils.isEmpty(org.getOrgCode())) {
      if (org.getOrgCode().length() != 4) {
        throw new RuntimeException("机构编码必须为4位");
      }
      
      // 验证编码是否符合级别规则
      char levelChar = org.getOrgCode().charAt(0);
      if (levelChar < '1' || levelChar > '3') {
        throw new RuntimeException("机构编码必须以1、2或3开头");
      }
      
      // 验证编码是否唯一
      Optional<Organization> existingOrg = organizationRepository.findByOrgCode(org.getOrgCode());
      if (existingOrg.isPresent()) {
        // 如果是更新操作，需要排除自己
        if (org.getOrgId() == null || !existingOrg.get().getOrgId().equals(org.getOrgId())) {
          throw new RuntimeException("机构编码已存在");
        }
      }
    }
  }

  /**
   * 启用/禁用机构
   */
  public Organization toggleStatus(Long orgId, Integer status) {
    Organization org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new RuntimeException("机构不存在"));
    org.setStatus(status);
    org.setUpdateTime(LocalDateTime.now());
    log.info("机构 {} 状态更新为 {}", org.getOrgName(), status == 1 ? "启用" : "禁用");
    return organizationRepository.save(org);
  }

  /**
   * 根据id获取机构
   */
  public Organization getOrgById(Long orgId) {
    return organizationRepository.findById(orgId)
        .orElseThrow(() -> new RuntimeException("机构不存在"));
  }

  /**
   * 根据编码获取机构
   */
  public Organization getOrgByCode(String orgCode) {
    return organizationRepository.findByOrgCode(orgCode)
        .orElseThrow(() -> new RuntimeException("机构不存在"));
  }
  
  /**
   * 根据机构编码获取级别
   */
  public Integer getLevelByOrgCode(String orgCode) {
    if (StringUtils.isEmpty(orgCode) || orgCode.length() != 4) {
      return null;
    }
    
    try {
      char levelChar = orgCode.charAt(0);
      int level = Character.getNumericValue(levelChar);
      if (level >= 1 && level <= 3) {
        return level;
      }
    } catch (Exception e) {
      log.warn("无法从编码获取级别: {}", orgCode);
    }
    
    return null;
  }

  /**
   * 检查同一父级下机构名称是否重复
   * @param parentId 父级机构ID（对于一级机构，parentId为null）
   * @param orgName 机构名称
   * @param excludeOrgId 排除的机构ID（用于更新操作）
   * @return 是否重复
   */
  public boolean isNameDuplicate(Long parentId, String orgName, Long excludeOrgId) {
    if (!StringUtils.hasText(orgName)) {
      return false;
    }
    
    // 查询同一父级下同名机构
    List<Organization> sameNameOrgs;
    if (parentId == null) {
      // 一级机构：按级别和名称查询
      sameNameOrgs = organizationRepository.findByOrgLevelAndOrgName(1, orgName);
    } else {
      // 二级或三级机构：按父级和名称查询
      sameNameOrgs = organizationRepository.findByParIdAndOrgName(parentId, orgName);
    }
    
    // 如果是更新操作，排除自身
    if (excludeOrgId != null) {
      sameNameOrgs = sameNameOrgs.stream()
          .filter(org -> !org.getOrgId().equals(excludeOrgId))
          .collect(Collectors.toList());
    }
    
    return !sameNameOrgs.isEmpty();
  }

  /**
   * 获取机构的完整层级路径
   * @param orgId 机构ID
   * @return 层级路径，如：总公司/技术部/Java开发组
   */
  public String getOrgPath(Long orgId) {
    Organization org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new RuntimeException("机构不存在"));
    
    List<String> pathNames = new ArrayList<>();
    pathNames.add(org.getOrgName());
    
    // 向上追溯父级机构
    Long currentParId = org.getParId();
    while (currentParId != null) {
      Organization parent = organizationRepository.findById(currentParId).orElse(null);
      if (parent != null) {
        pathNames.add(0, parent.getOrgName());
        currentParId = parent.getParId();
      } else {
        break;
      }
    }
    
    return String.join("/", pathNames);
  }

  /**
   * 获取某个一级机构下的所有二级机构
   */
  public List<Organization> getSecondLevelOrgsByFirst(Long firstOrgId) {
    return organizationRepository.findByParId(firstOrgId);
  }

  /**
   * 获取某个二级机构下的所有三级机构
   */
  public List<Organization> getThirdLevelOrgsBySecond(Long secondOrgId) {
    return organizationRepository.findByParId(secondOrgId);
  }

  /**
   * 获取机构可以删除
   */
  public boolean canDeleteOrg(Long orgId) {
    // 检查是否有子机构
    List<Organization> childOrgs = organizationRepository.findByParId(orgId);
    if (childOrgs != null && !childOrgs.isEmpty()) {
      return false;
    }
    return true;
  }

  // 分页获取机构列表
  public Page<Organization> getOrgsByLevelWithPage(Integer orgLevel, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
    if (orgLevel == null) {
      return organizationRepository.findAll(pageable);
    }
    return organizationRepository.findByOrgLevel(orgLevel, pageable);
  }
    
  // 分页获取机构树数据（扁平化）
  public Page<Organization> getAllOrgsWithPage(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("orgLevel", "orgCode"));
    return organizationRepository.findAll(pageable);
  }
}
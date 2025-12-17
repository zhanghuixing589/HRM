package org.example.hrm.service;

import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.UserQueryDTO;
import org.example.hrm.dto.UserRoleAssignDTO;
import org.example.hrm.entity.*;
import org.example.hrm.exception.BusinessException;
import org.example.hrm.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AdminUserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserRoleRepository userRoleRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private PositionRepository positionRepository;

  @Autowired
  private OrganizationService organizationService;

  /**
   * 查询员工列表（排除系统管理员自己）
   */
  public Page<User> queryEmployees(UserQueryDTO queryDTO, Long currentUserId, Pageable pageable) {
    Specification<User> spec = buildQuerySpecification(queryDTO, currentUserId);
    Page<User> page = userRepository.findAll(spec, pageable);

    // 补充关联信息
    page.getContent().forEach(this::enrichUserWithRelatedInfo);

    return page;
  }

  /**
   * 构建查询条件 - 简化版
   */
  private Specification<User> buildQuerySpecification(UserQueryDTO queryDTO, Long excludeUserId) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 排除当前系统管理员自己
      if (excludeUserId != null) {
        predicates.add(criteriaBuilder.notEqual(root.get("userId"), excludeUserId));
      }

      // 按状态查询，如果不指定状态，则查询所有状态（1-在职，0-离职，2-禁用）
      if (queryDTO.getStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
      } else {
        // 查询所有状态（1, 0, 2）
        predicates.add(criteriaBuilder.in(root.get("status")).value(Arrays.asList(1, 0, 2)));
      }

      // 按工号模糊查询
      if (StringUtils.hasText(queryDTO.getUserCode())) {
        predicates.add(criteriaBuilder.like(root.get("userCode"),
            "%" + queryDTO.getUserCode() + "%"));
      }

      // 按姓名模糊查询
      if (StringUtils.hasText(queryDTO.getUserName())) {
        predicates.add(criteriaBuilder.like(root.get("userName"),
            "%" + queryDTO.getUserName() + "%"));
      }

      // 按职位ID查询
      if (queryDTO.getPosId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("posId"), queryDTO.getPosId()));
      }

      // 按角色类型查询
      if (queryDTO.getRoleType() != null) {
        predicates.add(criteriaBuilder.equal(root.get("roleType"), queryDTO.getRoleType()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * 补充用户关联信息，包括机构路径
   */
  private void enrichUserWithRelatedInfo(User user) {
    try {
      // 补充机构名称和路径
      if (user.getOrgId() != null) {
        Organization org = organizationRepository.findById(user.getOrgId()).orElse(null);
        if (org != null) {
          user.setOrgName(org.getOrgName());

          // 获取机构完整路径（如：总公司/技术中心/前端组）
          String orgPath = getOrgPath(org.getOrgId());
          user.setOrgPath(orgPath);
        }
      }

      // 补充职位名称
      if (user.getPosId() != null) {
        Position position = positionRepository.findById(user.getPosId()).orElse(null);
        if (position != null) {
          user.setPositionName(position.getPosName());
        }
      }

      // 补充角色信息
      if (user.getRoleType() != null) {
        user.setRoleTypeName(user.getRoleTypeName());
      }

      // 补充状态名称
      user.setStatusName(getStatusText(user.getStatus()));

    } catch (Exception e) {
      log.error("补充用户关联信息失败", e);
    }
  }

  /**
   * 获取机构完整路径
   */
  private String getOrgPath(Long orgId) {
    try {
      return organizationService.getOrgPath(orgId);
    } catch (Exception e) {
      log.error("获取机构路径失败，机构ID: {}", orgId, e);

      // 如果获取失败，尝试手动构建
      try {
        Organization org = organizationRepository.findById(orgId).orElse(null);
        if (org == null)
          return "未知机构";

        return org.getOrgName();
      } catch (Exception ex) {
        return "机构信息获取失败";
      }
    }
  }

  /**
   * 获取用户详情
   */
  public User getUserDetail(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在"));

    enrichUserWithRelatedInfo(user);
    return user;
  }

  /**
   * 为用户分配角色
   */
  public User assignRoles(UserRoleAssignDTO assignDTO, Long operatorId) {
    log.info("为用户分配角色，用户ID: {}, 角色类型: {}, 操作人: {}",
        assignDTO.getUserId(), assignDTO.getRoleType(), operatorId);

    // 验证用户是否存在
    User user = userRepository.findById(assignDTO.getUserId())
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在"));

    // 验证角色类型是否合法（1-6）
    if (assignDTO.getRoleType() < 1 || assignDTO.getRoleType() > 6) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "角色类型无效");
    }

    // 检查是否为自己分配角色
    if (assignDTO.getUserId().equals(operatorId)) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能为自己分配角色");
    }

    // 获取角色ID
    Role role = roleRepository.findByRoleType(assignDTO.getRoleType())
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在"));

    // 删除用户原有的角色关联
    userRoleRepository.deleteByUserId(user.getUserId());

    // 创建新的角色关联
    UserRole userRole = new UserRole();
    userRole.setUserId(user.getUserId());
    userRole.setRoleId(role.getRoleId());
    userRoleRepository.save(userRole);

    // 更新用户的主角色类型
    user.setRoleType(assignDTO.getRoleType());
    user.setUpdateTime(LocalDateTime.now());

    User savedUser = userRepository.save(user);

    // 记录操作日志
    log.info("操作人 {} 为用户 {} 分配角色: {}",
        operatorId, savedUser.getUserCode(), savedUser.getRoleTypeName());

    return savedUser;
  }

  /**
   * 修改用户状态
   * 可以：1. 启用（status=1）2. 禁用（status=2）
   */
  public User updateUserStatus(Long userId, Integer status, Long operatorId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在"));

    // 不能修改自己的状态
    if (userId.equals(operatorId)) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能修改自己的状态");
    }

    // 验证状态值
    if (status != 1 && status != 2) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值无效，只能是1(在职)或2(禁用)");
    }

    Integer oldStatus = user.getStatus();
    user.setStatus(status);
    user.setUpdateTime(LocalDateTime.now());

    User savedUser = userRepository.save(user);

    String statusText = getStatusText(status);
    log.info("操作人 {} 修改用户 {} 状态: {} -> {}",
        operatorId, savedUser.getUserCode(), getStatusText(oldStatus), statusText);

    return savedUser;
  }

  /**
   * 恢复禁用用户（将状态从2改为1）
   */
  public User restoreUser(Long userId, Long operatorId) {
    return updateUserStatus(userId, 1, operatorId);
  }

  /**
   * 禁用用户（将状态改为2）
   */
  public User disableUser(Long userId, Long operatorId) {
    return updateUserStatus(userId, 2, operatorId);
  }

  /**
   * 获取所有角色列表
   */
  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }

  /**
   * 获取所有职位列表
   */
  public List<Position> getAllPositions() {
    return positionRepository.findAll();
  }

  /**
   * 获取状态文本
   */
  private String getStatusText(Integer status) {
    if (status == null)
      return "未知";
    switch (status) {
      case 1:
        return "在职";
      case 2:
        return "禁用";
      default:
        return "未知";
    }
  }
}
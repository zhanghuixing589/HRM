package org.example.hrm.controller;

import org.example.hrm.common.Result;
import org.example.hrm.dto.UserQueryDTO;
import org.example.hrm.dto.UserRoleAssignDTO;
import org.example.hrm.entity.Position;
import org.example.hrm.entity.Role;
import org.example.hrm.entity.User;
import org.example.hrm.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Slf4j
public class AdminUserController {

  @Autowired
  private AdminUserService adminUserService;

  /**
   * 查询员工列表
   */
  @GetMapping
  public Result queryEmployees(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      UserQueryDTO queryDTO) {

    // 获取当前登录用户ID（系统管理员）
    Long currentUserId = getCurrentUserId();

    // 构建分页
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

    Page<User> userPage = adminUserService.queryEmployees(queryDTO, currentUserId, pageable);

    return Result.success(userPage);
  }

  /**
   * 获取用户详情
   */
  @GetMapping("/{userId}")
  public Result getUserDetail(@PathVariable Long userId) {
    User user = adminUserService.getUserDetail(userId);
    return Result.success(user);
  }

  /**
   * 为用户分配角色
   */
  @PostMapping("/assign-role")
  public Result assignRole(@Valid @RequestBody UserRoleAssignDTO assignDTO) {
    Long currentUserId = getCurrentUserId();
    User user = adminUserService.assignRoles(assignDTO, currentUserId);
    return Result.success(user);
  }

  /**
   * 恢复禁用的用户
   */
  @PutMapping("/{userId}/restore")
  public Result restoreUser(@PathVariable Long userId) {
    Long currentUserId = getCurrentUserId();
    User user = adminUserService.restoreUser(userId, currentUserId);
    return Result.success(user);
  }

  /**
   * 禁用用户
   */
  @PutMapping("/{userId}/disable")
  public Result disableUser(@PathVariable Long userId) {
    Long currentUserId = getCurrentUserId();
    User user = adminUserService.disableUser(userId, currentUserId);
    return Result.success(user);
  }

  /**
   * 获取所有角色列表
   */
  @GetMapping("/roles")
  public Result getAllRoles() {
    List<Role> roles = adminUserService.getAllRoles();
    return Result.success(roles);
  }

  /**
   * 获取所有职位列表
   */
  @GetMapping("/positions")
  public Result getAllPositions() {
    List<Position> positions = adminUserService.getAllPositions();
    return Result.success(positions);
  }

  /**
   * 获取当前登录用户ID
   */
  private Long getCurrentUserId() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String userCode = auth.getName();

      // 这里需要根据实际情况获取用户ID
      // 你可以添加一个UserService方法来根据userCode获取userId
      // 或者从token中解析userId

      // 临时返回一个默认值，实际应用中需要根据认证信息获取
      return 1L; // 系统管理员ID
    } catch (Exception e) {
      log.error("获取当前用户ID失败", e);
      return null;
    }
  }
}
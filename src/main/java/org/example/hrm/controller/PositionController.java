package org.example.hrm.controller;

import org.example.hrm.common.Result;
import org.example.hrm.entity.Position;
import org.example.hrm.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/hr/position")
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('HR_MANAGER')") // 只有人事经理可以操作
public class PositionController {

  @Autowired
  private PositionService positionService;

  // 获取所有职位
  @GetMapping("/list")
  public Result<List<Position>> getAllPositions() {
    try {
      List<Position> positions = positionService.getAllPositions();
      return Result.success(positions);
    } catch (Exception e) {
      log.error("获取职位列表失败", e);
      return Result.error("获取职位列表失败：" + e.getMessage());
    }
  }

  // 根据机构id获取职位
  @GetMapping("/org/{orgId}")
  public Result<List<Position>> getPositionsByOrgId(@PathVariable Long orgId) {
    try {
      List<Position> positions = positionService.getPositionsByOrgId(orgId);
      return Result.success(positions);
    } catch (Exception e) {
      log.error("获取职位失败", e);
      return Result.error("获取职位失败：" + e.getMessage());
    }
  }

  // 根据状态获取职位
  @GetMapping("/status/{status}")
  public Result<List<Position>> getPositionsByStatus(@PathVariable Integer status) {
    try {
      List<Position> positions = positionService.getPositionsByStatus(status);
      return Result.success(positions);
    } catch (Exception e) {
      log.error("获取职位失败", e);
      return Result.error("获取职位失败：" + e.getMessage());
    }
  }

  // 创建或更新职位
  @PostMapping("/save")
  public Result<Position> savePosition(@RequestBody Position position) {
    try {
      Position savedPosition = positionService.savePosition(position);
      return Result.success("职位保存成功", savedPosition);
    } catch (Exception e) {
      log.error("保存职位失败", e);
      return Result.error("保存职位失败：" + e.getMessage());
    }
  }

  // 删除职位
  @DeleteMapping("/{posId}")
  public Result<String> deletePosition(@PathVariable Long posId) {
    try {
      positionService.deletePosition(posId);
      return Result.success("职位删除成功");
    } catch (Exception e) {
      log.error("删除职位失败", e);
      return Result.error("删除职位失败：" + e.getMessage());
    }
  }

  // 启用或禁用职位
  @PutMapping("/{posId}/status/{status}")
  public Result<Position> toggleStatus(@PathVariable Long posId, @PathVariable Integer status) {
    try {
      Position position = positionService.toggleStatus(posId, status);
      return Result.success("职位状态更新成功", position);
    } catch (Exception e) {
      log.error("更新职位状态失败", e);
      return Result.error("更新职位状态失败：" + e.getMessage());
    }
  }

  // 根据id批量查询职位
  @PostMapping("/by-org-ids")
  public Result<List<Position>> getPositionsByOrgIds(@RequestBody List<Long> orgIds) {
    try {
      List<Position> positions = positionService.getPositionsByOrgIds(orgIds);
      return Result.success(positions);
    } catch (Exception e) {
      log.error("批量获取职位失败", e);
      return Result.error("批量获取职位失败：" + e.getMessage());
    }
  }

  /**
   * 分页获取所有职位
   */
  @GetMapping("/page")
  public Result<Page<Position>> getAllPositionsPage(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      Page<Position> positions = positionService.getAllPositionsWithPage(page, size);
      return Result.success(positions);
    } catch (Exception e) {
      log.error("分页获取职位列表失败", e);
      return Result.error("分页获取职位列表失败：" + e.getMessage());
    }
  }

  /**
   * 根据机构id分页获取职位
   */
  @GetMapping("/org/{orgId}/page")
  public Result<Page<Position>> getPositionsByOrgIdPage(
      @PathVariable Long orgId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      Page<Position> positions = positionService.getPositionsByOrgIdWithPage(orgId, page, size);
      return Result.success(positions);
    } catch (Exception e) {
      log.error("分页获取职位失败", e);
      return Result.error("分页获取职位失败：" + e.getMessage());
    }
  }

  /**
   * 根据状态分页获取职位
   */
  @GetMapping("/status/{status}/page")
  public Result<Page<Position>> getPositionsByStatusPage(
      @PathVariable Integer status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      Page<Position> positions = positionService.getPositionsByStatusWithPage(status, page, size);
      return Result.success(positions);
    } catch (Exception e) {
      log.error("分页获取职位失败", e);
      return Result.error("分页获取职位失败：" + e.getMessage());
    }
  }
}

package org.example.hrm.controller;

import org.example.hrm.common.Result;
import org.example.hrm.entity.Position;
import org.example.hrm.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hr/position")
@CrossOrigin
@Slf4j
public class PositionController {

  @Autowired
  private PositionService positionService;

  // ========== 新增：获取职位名称相关接口 ==========
  
  /**
   * 根据职位ID获取职位名称（简版）
   * GET /hr/position/{posId}/name
   * 示例：GET /hr/position/1/name
   */
  @GetMapping("/{posId}/name")
  public Result<Map<String, String>> getPositionNameById(@PathVariable Long posId) {
    try {
      log.info("获取职位名称，职位ID: {}", posId);
      Position position = positionService.getPositionById(posId);
      
      if (position == null) {
        return Result.error("职位不存在");
      }
      
      Map<String, String> result = new HashMap<>();
      result.put("posId", String.valueOf(position.getPosId()));
      result.put("posName", position.getPosName());
      result.put("posCode", position.getPosCode());
      
      return Result.success("获取职位名称成功", result);
    } catch (Exception e) {
      log.error("获取职位名称失败，posId: {}", posId, e);
      return Result.error("获取职位名称失败：" + e.getMessage());
    }
  }
  
  /**
   * 批量根据职位ID获取职位名称
   * POST /hr/position/batch-names
   * 请求体：{"posIds": [1, 2, 3]}
   */
  @PostMapping("/batch-names")
  public Result<Map<Long, String>> getBatchPositionNames(@RequestBody Map<String, List<Long>> request) {
    try {
      List<Long> posIds = request.get("posIds");
      if (posIds == null || posIds.isEmpty()) {
        return Result.success("职位ID列表为空", new HashMap<>());
      }
      
      log.info("批量获取职位名称，职位ID列表: {}", posIds);
      
      // 批量查询职位
      List<Position> positions = positionService.getAllPositions();
      Map<Long, String> result = new HashMap<>();
      
      // 过滤出需要的职位
      for (Position position : positions) {
        if (posIds.contains(position.getPosId())) {
          result.put(position.getPosId(), position.getPosName());
        }
      }
      
      // 检查是否有不存在的职位ID
      for (Long posId : posIds) {
        if (!result.containsKey(posId)) {
          result.put(posId, "未知职位");
        }
      }
      
      return Result.success("批量获取职位名称成功", result);
    } catch (Exception e) {
      log.error("批量获取职位名称失败", e);
      return Result.error("批量获取职位名称失败：" + e.getMessage());
    }
  }
  
  /**
   * 获取所有启用的职位列表（名称和ID）
   * GET /hr/position/active-names
   */
  @GetMapping("/active-names")
  public Result<List<Map<String, Object>>> getActivePositionNames() {
    try {
      log.info("获取所有启用的职位名称列表");
      List<Position> positions = positionService.getActivePositions();
      
      List<Map<String, Object>> result = positions.stream()
          .map(position -> {
            Map<String, Object> map = new HashMap<>();
            map.put("posId", position.getPosId());
            map.put("posName", position.getPosName());
            map.put("posCode", position.getPosCode());
            map.put("orgName", position.getOrgName());
            return map;
          })
          .collect(Collectors.toList());
      
      return Result.success("获取启用职位列表成功", result);
    } catch (Exception e) {
      log.error("获取启用职位列表失败", e);
      return Result.error("获取启用职位列表失败：" + e.getMessage());
    }
  }
  
  /**
   * 根据职位编码获取职位名称
   * GET /hr/position/code/{posCode}/name
   * 示例：GET /hr/position/code/DEV_JAVA/name
   */
  @GetMapping("/code/{posCode}/name")
  public Result<Map<String, String>> getPositionNameByCode(@PathVariable String posCode) {
    try {
      log.info("根据职位编码获取职位名称，职位编码: {}", posCode);
      Position position = positionService.getPositionByCode(posCode);
      
      Map<String, String> result = new HashMap<>();
      result.put("posId", String.valueOf(position.getPosId()));
      result.put("posName", position.getPosName());
      result.put("posCode", position.getPosCode());
      
      return Result.success("获取职位名称成功", result);
    } catch (Exception e) {
      log.error("根据职位编码获取职位名称失败，posCode: {}", posCode, e);
      return Result.error("获取职位名称失败：" + e.getMessage());
    }
  }

  // ========== 原有方法保持不变 ==========
  
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
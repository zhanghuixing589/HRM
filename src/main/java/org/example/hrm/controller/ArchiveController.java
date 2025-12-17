package org.example.hrm.controller;

import org.example.hrm.common.PageResult;
import org.example.hrm.common.Result;
import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.ArchiveDTO;
import org.example.hrm.dto.ArchiveQueryDTO;
import org.example.hrm.dto.ReviewDTO;
import org.example.hrm.entity.Archive;
import org.example.hrm.entity.ArchiveOperation;
import org.example.hrm.exception.BusinessException;
import org.example.hrm.service.ArchiveChangeService;
import org.example.hrm.service.ArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/archive")
@CrossOrigin
@Slf4j
public class ArchiveController {

  @Autowired
  private ArchiveService archiveService;

  @Autowired
  private ArchiveChangeService archiveChangeService;

  /**
   * 登记档案（人事专员）
   */
  @PostMapping("/register")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<Archive> registerArchive(@RequestBody ArchiveDTO archiveDTO,
      @RequestAttribute Long userId) {
    try {
      Archive archive = archiveService.registerArchive(archiveDTO, userId);
      return Result.success("档案登记成功，等待复核", archive);
    } catch (BusinessException e) {
      log.error("档案登记失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("档案登记失败", e);
      return Result.error(ResultCode.ERROR, "档案登记失败");
    }
  }

  /**
   * 复核档案（人事经理）
   */
  @PostMapping("/review")
  @PreAuthorize("hasRole('HR_MANAGER')")
  public Result<Archive> reviewArchive(@RequestBody ReviewDTO reviewDTO,
      @RequestAttribute Long userId) {
    try {
      Archive archive = archiveService.reviewArchive(reviewDTO, userId);
      String message = reviewDTO.getAction() == 2 ? "档案审核通过" : "档案已驳回";
      return Result.success(message, archive);
    } catch (BusinessException e) {
      log.error("档案复核失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("档案复核失败", e);
      return Result.error(ResultCode.ERROR, "档案复核失败");
    }
  }

  /**
   * 重新提交档案（人事专员）
   */
  @PostMapping("/resubmit/{arcId}")
  @PreAuthorize("hasRole('HR_SPECIALIST')")
  public Result<Archive> resubmitArchive(@PathVariable Long arcId,
      @RequestBody ArchiveDTO archiveDTO,
      @RequestAttribute Long userId) {
    try {
      Archive archive = archiveService.resubmitArchive(arcId, archiveDTO, userId);
      return Result.success("档案重新提交成功，等待复核", archive);
    } catch (BusinessException e) {
      log.error("档案重新提交失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("档案重新提交失败", e);
      return Result.error(ResultCode.ERROR, "档案重新提交失败");
    }
  }

  /**
   * 删除档案（标记删除）
   */
  @DeleteMapping("/{arcId}")
  // @PreAuthorize("hasRole('HR_MANAGER')")
  public Result<String> deleteArchive(@PathVariable Long arcId,
      @RequestAttribute Long userId) {
    try {
      archiveService.deleteArchive(arcId, userId);
      return Result.success("档案删除成功");
    } catch (BusinessException e) {
      log.error("删除档案失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("删除档案失败", e);
      return Result.error(ResultCode.ERROR, "删除档案失败");
    }
  }

  /**
   * 恢复已删除的档案
   */
  @PutMapping("/restore/{arcId}")
  // @PreAuthorize("hasRole('HR_MANAGER')")
  public Result<Archive> restoreArchive(@PathVariable Long arcId,
      @RequestAttribute Long userId) {
    try {
      Archive archive = archiveService.restoreArchive(arcId, userId);
      return Result.success("档案恢复成功", archive);
    } catch (BusinessException e) {
      log.error("恢复档案失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("恢复档案失败", e);
      return Result.error(ResultCode.ERROR, "恢复档案失败");
    }
  }

  /**
   * 获取档案详情
   */
  @GetMapping("/detail/{arcId}")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<Archive> getArchiveDetail(@PathVariable Long arcId) {
    try {
      Archive archive = archiveService.getArchiveDetail(arcId);
      return Result.success(archive);
    } catch (BusinessException e) {
      log.error("获取档案详情失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("获取档案详情失败", e);
      return Result.error(ResultCode.ERROR, "获取档案详情失败");
    }
  }

  /**
   * 分页查询档案
   */
  @PostMapping("/query")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<PageResult<Archive>> queryArchives(@RequestBody ArchiveQueryDTO queryDTO) {
    try {
      PageRequest pageable = PageRequest.of(
          queryDTO.getPage() != null ? queryDTO.getPage() : 0,
          queryDTO.getSize() != null ? queryDTO.getSize() : 10,
          Sort.by(Sort.Direction.DESC, "createTime"));

      Page<Archive> page = archiveService.queryArchives(queryDTO, pageable);

      PageResult<Archive> pageResult = PageResult.success(
          page.getNumber(),
          page.getSize(),
          page.getTotalElements(),
          page.getContent());

      return Result.success(pageResult);
    } catch (Exception e) {
      log.error("查询档案失败", e);
      return Result.error(ResultCode.ERROR, "查询档案失败");
    }
  }

  /**
   * GET方式查询档案（支持分页和筛选）
   */
  @GetMapping("/query-list")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<PageResult<Archive>> queryArchivesGet(
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long firstOrgId,
      @RequestParam(required = false) Long secondOrgId,
      @RequestParam(required = false) Long thirdOrgId,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String arcCode,
      @RequestParam(required = false) String idCard,
      @RequestParam(required = false) String positionName,
      @RequestParam(required = false) String createTimeStart,
      @RequestParam(required = false) String createTimeEnd,
      @RequestParam(required = false) Long writeId,
      @RequestParam(required = false) Long reviewId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      // 构建查询DTO
      ArchiveQueryDTO queryDTO = new ArchiveQueryDTO();
      queryDTO.setStatus(status);
      queryDTO.setFirstOrgId(firstOrgId);
      queryDTO.setSecondOrgId(secondOrgId);
      queryDTO.setThirdOrgId(thirdOrgId);
      queryDTO.setName(name);
      queryDTO.setArcCode(arcCode);
      queryDTO.setIdCard(idCard);
      queryDTO.setPositionName(positionName);
      queryDTO.setWriteId(writeId);
      queryDTO.setReviewId(reviewId);
      queryDTO.setPage(page);
      queryDTO.setSize(size);

      // 处理时间参数
      if (createTimeStart != null) {
        try {
          LocalDateTime startTime = LocalDateTime.parse(createTimeStart + "T00:00:00");
          queryDTO.setCreateTimeStart(startTime);
        } catch (Exception e) {
          // 如果解析失败，忽略时间参数
          log.warn("创建时间开始参数解析失败: {}", createTimeStart);
        }
      }

      if (createTimeEnd != null) {
        try {
          LocalDateTime endTime = LocalDateTime.parse(createTimeEnd + "T23:59:59");
          queryDTO.setCreateTimeEnd(endTime);
        } catch (Exception e) {
          // 如果解析失败，忽略时间参数
          log.warn("创建时间结束参数解析失败: {}", createTimeEnd);
        }
      }

      PageRequest pageable = PageRequest.of(
          page, size, Sort.by(Sort.Direction.DESC, "createTime"));

      Page<Archive> pageResult = archiveService.queryArchives(queryDTO, pageable);

      PageResult<Archive> result = PageResult.success(
          pageResult.getNumber(),
          pageResult.getSize(),
          pageResult.getTotalElements(),
          pageResult.getContent());

      return Result.success(result);
    } catch (Exception e) {
      log.error("查询档案失败", e);
      return Result.error(ResultCode.ERROR, "查询档案失败: " + e.getMessage());
    }
  }

  /**
   * 获取待复核档案列表
   */
  @GetMapping("/pending-review")
  @PreAuthorize("hasRole('HR_MANAGER')")
  public Result<List<Archive>> getPendingReviewArchives() {
    try {
      List<Archive> archives = archiveService.getPendingReviewArchives();
      return Result.success(archives);
    } catch (Exception e) {
      log.error("获取待复核档案失败", e);
      return Result.error(ResultCode.ERROR, "获取待复核档案失败");
    }
  }

  /**
   * 获取档案审核流程
   */
  @GetMapping("/process/{arcId}")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<List<ArchiveOperation>> getArchiveProcess(@PathVariable Long arcId) {
    try {
      List<ArchiveOperation> process = archiveService.getArchiveProcess(arcId);
      return Result.success(process);
    } catch (Exception e) {
      log.error("获取档案流程失败", e);
      return Result.error(ResultCode.ERROR, "获取档案流程失败");
    }
  }

  /**
   * 获取档案统计信息
   */
  @GetMapping("/stats")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<Map<String, Long>> getArchiveStats() {
    try {
      Map<String, Long> stats = archiveService.getArchiveStats();
      return Result.success(stats);
    } catch (Exception e) {
      log.error("获取档案统计失败", e);
      return Result.error(ResultCode.ERROR, "获取档案统计失败");
    }
  }

  /**
 * 获取我的档案提交记录（支持分页和条件查询）
 */
@GetMapping("/my-archives")
@PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
public Result<PageResult<Archive>> getMyArchives(
    @RequestParam(required = false) String arcCode,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String positionName,
    @RequestParam(required = false) Integer status,
    @RequestAttribute Long userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {

  try {
    // 构建查询参数
    Map<String, Object> params = new HashMap<>();
    params.put("writeId", userId);

    if (StringUtils.hasText(arcCode)) {
      params.put("arcCode", arcCode);
    }
    if (StringUtils.hasText(name)) {
      params.put("name", name);
    }
    if (StringUtils.hasText(positionName)) {
      params.put("positionName", positionName);
    }
    if (status != null) {
      params.put("status", status);
    }

    Page<Archive> pageResult = archiveService.queryMyArchives(params, page, size);

    // 重要：补充机构名称和用户名称
    List<Archive> archives = pageResult.getContent();
    for (Archive archive : archives) {
      // 补充机构名称
      archiveService.enrichArchiveWithOrgNames(archive);
      // 补充用户名称
      archiveService.enrichArchiveWithUserNames(archive);
    }

    PageResult<Archive> result = PageResult.success(
        pageResult.getNumber(),
        pageResult.getSize(),
        pageResult.getTotalElements(),
        archives);

    return Result.success(result);
  } catch (Exception e) {
    log.error("获取用户档案失败", e);
    return Result.error(ResultCode.ERROR, "获取用户档案失败");
  }
}
}
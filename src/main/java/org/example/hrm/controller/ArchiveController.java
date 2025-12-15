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
import org.example.hrm.service.ArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/archive")
@CrossOrigin
@Slf4j
public class ArchiveController {

  @Autowired
  private ArchiveService archiveService;

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
  @PreAuthorize("hasRole('HR_MANAGER')")
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
  @PreAuthorize("hasRole('HR_MANAGER')")
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
   * 获取当前用户登记的档案
   */
  @GetMapping("/my-archives")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<List<Archive>> getMyArchives(@RequestAttribute Long userId) {
    try {
      List<Archive> archives = archiveService.getArchivesByWriter(userId);
      return Result.success(archives);
    } catch (Exception e) {
      log.error("获取用户档案失败", e);
      return Result.error(ResultCode.ERROR, "获取用户档案失败");
    }
  }
}
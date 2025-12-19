package org.example.hrm.controller;

import org.example.hrm.common.PageResult;
import org.example.hrm.common.Result;
import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.ArchiveChangeDTO;
import org.example.hrm.dto.ArchiveChangeQueryDTO;
import org.example.hrm.dto.ReviewChangeDTO;
import org.example.hrm.entity.ArchiveChange;
import org.example.hrm.entity.ChangeOperation;
import org.example.hrm.exception.BusinessException;
import org.example.hrm.service.ArchiveChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/change")
@CrossOrigin
@Slf4j
public class ArchiveChangeController {

  @Autowired
  private ArchiveChangeService archiveChangeService;

  /**
   * 申请档案变更（人事专员）
   */
  @PostMapping("/apply")
  // @PreAuthorize("hasRole('HR_SPECIALIST')")
  public Result<ArchiveChange> applyChange(@RequestBody ArchiveChangeDTO changeDTO,
      @RequestAttribute Long userId) {
    try {
      ArchiveChange archiveChange = archiveChangeService.applyArchiveChange(changeDTO, userId);
      return Result.success("档案变更申请已提交，等待复核", archiveChange);
    } catch (BusinessException e) {
      log.error("档案变更申请失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("档案变更申请失败", e);
      return Result.error(ResultCode.ERROR, "档案变更申请失败");
    }
  }

  /**
   * 审核变更申请（人事经理）
   */
  @PostMapping("/review")
  // @PreAuthorize("hasRole('HR_MANAGER')")
  public Result<ArchiveChange> reviewChange(@RequestBody ReviewChangeDTO reviewDTO,
      @RequestAttribute Long userId) {
    try {
      ArchiveChange archiveChange = archiveChangeService.reviewChange(reviewDTO, userId);
      String message = reviewDTO.getAction() == 2 ? "变更申请审核通过" : "变更申请已驳回";
      return Result.success(message, archiveChange);
    } catch (BusinessException e) {
      log.error("变更申请审核失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("变更申请审核失败", e);
      return Result.error(ResultCode.ERROR, "变更申请审核失败");
    }
  }

  /**
   * 重新提交变更申请（人事专员）
   */
  @PostMapping("/resubmit/{changeId}")
  @PreAuthorize("hasRole('HR_SPECIALIST')")
  public Result<ArchiveChange> resubmitChange(@PathVariable Long changeId,
      @RequestBody ArchiveChangeDTO changeDTO,
      @RequestAttribute Long userId) {
    try {
      ArchiveChange archiveChange = archiveChangeService.resubmitChange(changeId, changeDTO, userId);
      return Result.success("变更申请重新提交成功，等待复核", archiveChange);
    } catch (BusinessException e) {
      log.error("变更申请重新提交失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("变更申请重新提交失败", e);
      return Result.error(ResultCode.ERROR, "变更申请重新提交失败");
    }
  }

  /**
   * 获取变更申请详情
   */
  @GetMapping("/detail/{changeId}")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<ArchiveChange> getChangeDetail(@PathVariable Long changeId) {
    try {
      ArchiveChange archiveChange = archiveChangeService.getChangeDetail(changeId);
      return Result.success(archiveChange);
    } catch (BusinessException e) {
      log.error("获取变更申请详情失败", e);
      return Result.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
      log.error("获取变更申请详情失败", e);
      return Result.error(ResultCode.ERROR, "获取变更申请详情失败");
    }
  }

  /**
   * 分页查询变更申请
   */
  @PostMapping("/query")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<PageResult<ArchiveChange>> queryChanges(@RequestBody ArchiveChangeQueryDTO queryDTO) {
    try {
      PageRequest pageable = PageRequest.of(
          queryDTO.getPage() != null ? queryDTO.getPage() : 0,
          queryDTO.getSize() != null ? queryDTO.getSize() : 10,
          Sort.by(Sort.Direction.DESC, "createTime"));

      Page<ArchiveChange> page = archiveChangeService.queryChanges(queryDTO, pageable);

      PageResult<ArchiveChange> pageResult = PageResult.success(
          page.getNumber(),
          page.getSize(),
          page.getTotalElements(),
          page.getContent());

      return Result.success(pageResult);
    } catch (Exception e) {
      log.error("查询变更申请失败", e);
      return Result.error(ResultCode.ERROR, "查询变更申请失败");
    }
  }

  /**
   * 获取待复核变更申请列表
   */
  @GetMapping("/pending-review")
  @PreAuthorize("hasRole('HR_MANAGER')")
  public Result<List<ArchiveChange>> getPendingReviewChanges() {
    try {
      List<ArchiveChange> changes = archiveChangeService.getPendingReviewChanges();
      return Result.success(changes);
    } catch (Exception e) {
      log.error("获取待复核变更申请失败", e);
      return Result.error(ResultCode.ERROR, "获取待复核变更申请失败");
    }
  }

  /**
   * 获取变更审核流程
   */
  @GetMapping("/process/{changeId}")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<List<ChangeOperation>> getChangeProcess(@PathVariable Long changeId) {
    try {
      List<ChangeOperation> process = archiveChangeService.getChangeProcess(changeId);
      return Result.success(process);
    } catch (Exception e) {
      log.error("获取变更流程失败", e);
      return Result.error(ResultCode.ERROR, "获取变更流程失败");
    }
  }

  /**
   * 根据档案ID获取变更历史
   */
  @GetMapping("/history/{arcId}")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<List<ArchiveChange>> getChangeHistory(@PathVariable Long arcId) {
    try {
      List<ArchiveChange> history = archiveChangeService.getChangeHistoryByArchive(arcId);
      return Result.success(history);
    } catch (Exception e) {
      log.error("获取变更历史失败", e);
      return Result.error(ResultCode.ERROR, "获取变更历史失败");
    }
  }

  /**
   * GET方式查询变更申请
   */
  @GetMapping("/query-list")
  @PreAuthorize("hasRole('HR_SPECIALIST') or hasRole('HR_MANAGER')")
  public Result<PageResult<ArchiveChange>> queryChangesGet(
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String archiveName,
      @RequestParam(required = false) String archiveCode,
      @RequestParam(required = false) String changeReason,
      @RequestParam(required = false) Long applyId,
      @RequestParam(required = false) Long reviewId,
      @RequestParam(required = false) String createTimeStart,
      @RequestParam(required = false) String createTimeEnd,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      // 构建查询DTO
      ArchiveChangeQueryDTO queryDTO = new ArchiveChangeQueryDTO();
      queryDTO.setStatus(status);
      queryDTO.setArchiveName(archiveName);
      queryDTO.setArchiveCode(archiveCode);
      queryDTO.setChangeReason(changeReason);
      queryDTO.setApplyId(applyId);
      queryDTO.setReviewId(reviewId);
      queryDTO.setPage(page);
      queryDTO.setSize(size);

      // 处理时间参数
      if (createTimeStart != null) {
        try {
          LocalDateTime startTime = LocalDateTime.parse(createTimeStart + "T00:00:00");
          queryDTO.setCreateTimeStart(startTime);
        } catch (Exception e) {
          log.warn("创建时间开始参数解析失败: {}", createTimeStart);
        }
      }

      if (createTimeEnd != null) {
        try {
          LocalDateTime endTime = LocalDateTime.parse(createTimeEnd + "T23:59:59");
          queryDTO.setCreateTimeEnd(endTime);
        } catch (Exception e) {
          log.warn("创建时间结束参数解析失败: {}", createTimeEnd);
        }
      }

      PageRequest pageable = PageRequest.of(
          page, size, Sort.by(Sort.Direction.DESC, "createTime"));

      Page<ArchiveChange> pageResult = archiveChangeService.queryChanges(queryDTO, pageable);

      PageResult<ArchiveChange> result = PageResult.success(
          pageResult.getNumber(),
          pageResult.getSize(),
          pageResult.getTotalElements(),
          pageResult.getContent());

      return Result.success(result);
    } catch (Exception e) {
      log.error("查询变更申请失败", e);
      return Result.error(ResultCode.ERROR, "查询变更申请失败: " + e.getMessage());
    }
  }

  /**
   * 获取我的变更记录
   */
  @GetMapping("/my-records")
  @PreAuthorize("hasRole('HR_SPECIALIST')")
  public Result<PageResult<ArchiveChange>> getMyChangeRecords(
      @RequestParam(required = false) String archiveCode,
      @RequestParam(required = false) String archiveName,
      @RequestParam(required = false) String changeReason,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String createTimeStart,
      @RequestParam(required = false) String createTimeEnd,
      @RequestAttribute Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    try {
      ArchiveChangeQueryDTO queryDTO = new ArchiveChangeQueryDTO();
      queryDTO.setApplyId(userId); // 只查询当前用户的记录
      queryDTO.setArchiveCode(archiveCode);
      queryDTO.setArchiveName(archiveName);
      queryDTO.setChangeReason(changeReason);
      queryDTO.setStatus(status);
      queryDTO.setPage(page);
      queryDTO.setSize(size);

      // 处理时间参数
      if (createTimeStart != null) {
        LocalDateTime startTime = LocalDateTime.parse(createTimeStart + "T00:00:00");
        queryDTO.setCreateTimeStart(startTime);
      }
      if (createTimeEnd != null) {
        LocalDateTime endTime = LocalDateTime.parse(createTimeEnd + "T23:59:59");
        queryDTO.setCreateTimeEnd(endTime);
      }

      PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
      Page<ArchiveChange> pageResult = archiveChangeService.queryChanges(queryDTO, pageable);

      PageResult<ArchiveChange> result = PageResult.success(
          pageResult.getNumber(),
          pageResult.getSize(),
          pageResult.getTotalElements(),
          pageResult.getContent());

      return Result.success(result);
    } catch (Exception e) {
      log.error("获取我的变更记录失败", e);
      return Result.error(ResultCode.ERROR, "获取变更记录失败");
    }
  }

  /**
   * 检查档案是否有待审核的变更申请
   */
  @GetMapping("/check-pending/{arcId}")
  @PreAuthorize("hasRole('HR_SPECIALIST')")
  public Result<Map<String, Object>> checkPendingChanges(@PathVariable Long arcId) {
    try {
      boolean hasPending = archiveChangeService.hasPendingChange(arcId);
      Map<String, Object> result = new HashMap<>();
      result.put("hasPending", hasPending);
      result.put("message", hasPending ? "该档案有待审核的变更申请" : "可以提交变更申请");
      return Result.success(result);
    } catch (Exception e) {
      log.error("检查待审核变更申请失败", e);
      return Result.error(ResultCode.ERROR, "检查失败");
    }
  }

}
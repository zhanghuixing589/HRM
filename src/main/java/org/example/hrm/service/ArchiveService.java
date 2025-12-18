package org.example.hrm.service;

import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.ArchiveDTO;
import org.example.hrm.dto.ArchiveQueryDTO;
import org.example.hrm.dto.ReviewDTO;
import org.example.hrm.entity.Archive;
import org.example.hrm.entity.ArchiveOperation;
import org.example.hrm.entity.ArchiveStandard;
import org.example.hrm.entity.Organization;
import org.example.hrm.entity.Position;
import org.example.hrm.entity.User;
import org.example.hrm.exception.BusinessException;
import org.example.hrm.repository.ArchiveOperationRepository;
import org.example.hrm.repository.ArchiveRepository;
import org.example.hrm.repository.ArchiveStandardRepository;
import org.example.hrm.repository.OrganizationRepository;
import org.example.hrm.repository.PositionRepository;
import org.example.hrm.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ArchiveService {

  @Autowired
  private ArchiveRepository archiveRepository;

  @Autowired
  private ArchiveOperationRepository archiveOperationRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private PositionRepository positionRepository; 

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ArchiveSalaryService archiveSalaryService;

  @Autowired
  private ArchiveStandardRepository archiveStandardRepository;

  // 生成档案编码
  private String generateArcCode(Long thirdOrgId) {
    log.info("=== 开始生成档案编码 ===");
    log.info("传入的三级机构ID: {}", thirdOrgId);

    // 获取三级机构信息
    Organization thirdOrg = organizationRepository.findById(thirdOrgId)
        .orElseThrow(() -> new BusinessException(ResultCode.ORG_NOT_EXIST,
            String.format("三级机构不存在，ID: %d", thirdOrgId)));

    log.info("找到三级机构: {} (ID: {}, Code: {}, Level: {}, ParId: {})",
        thirdOrg.getOrgName(), thirdOrg.getOrgId(), thirdOrg.getOrgCode(),
        thirdOrg.getOrgLevel(), thirdOrg.getParId());

    // 获取二级机构信息
    Long secondOrgId = thirdOrg.getParId();
    log.info("三级机构的父级ID: {}", secondOrgId);

    if (secondOrgId == null) {
      throw new BusinessException(ResultCode.ORG_NOT_EXIST, "三级机构没有指定父级机构");
    }

    Organization secondOrg = organizationRepository.findById(secondOrgId)
        .orElseThrow(() -> new BusinessException(ResultCode.ORG_NOT_EXIST,
            String.format("二级机构不存在，ID: %d", secondOrgId)));

    log.info("找到二级机构: {} (ID: {}, Code: {}, Level: {}, ParId: {})",
        secondOrg.getOrgName(), secondOrg.getOrgId(), secondOrg.getOrgCode(),
        secondOrg.getOrgLevel(), secondOrg.getParId());

    // 获取一级机构信息
    Long firstOrgId = secondOrg.getParId();
    log.info("二级机构的父级ID: {}", firstOrgId);

    if (firstOrgId == null) {
      throw new BusinessException(ResultCode.ORG_NOT_EXIST, "二级机构没有指定父级机构");
    }

    Organization firstOrg = organizationRepository.findById(firstOrgId)
        .orElseThrow(() -> new BusinessException(ResultCode.ORG_NOT_EXIST,
            String.format("一级机构不存在，ID: %d", firstOrgId)));

    log.info("找到一级机构: {} (ID: {}, Code: {}, Level: {}, ParId: {})",
        firstOrg.getOrgName(), firstOrg.getOrgId(), firstOrg.getOrgCode(),
        firstOrg.getOrgLevel(), firstOrg.getParId());

    // 验证机构编码格式（必须是4位）
    validateOrgCode(firstOrg.getOrgCode(), "一级机构");
    validateOrgCode(secondOrg.getOrgCode(), "二级机构");
    validateOrgCode(thirdOrg.getOrgCode(), "三级机构");

    // 获取机构编码的后2位
    String firstOrgCode = getLastTwoChars(firstOrg.getOrgCode());
    String secondOrgCode = getLastTwoChars(secondOrg.getOrgCode());
    String thirdOrgCode = getLastTwoChars(thirdOrg.getOrgCode());

    // 获取当前年份
    String year = String.valueOf(Year.now().getValue());

    // 获取当前三级机构下今年的档案数量
    Long count = archiveRepository.countByThirdOrgIdAndYear(thirdOrgId);
    if (count == null) {
      count = 0L;
    }
    log.info("当前三级机构 {} 下今年的档案数量: {}", thirdOrgId, count);

    // 生成顺序号（2位，从01开始）
    String sequence = String.format("%02d", count + 1);

    String arcCode = year + firstOrgCode + secondOrgCode + thirdOrgCode + sequence;
    log.info("生成的档案编码: {} = {} + {} + {} + {} + {}",
        arcCode, year, firstOrgCode, secondOrgCode, thirdOrgCode, sequence);

    // 验证生成的档案编码是否唯一
    if (archiveRepository.findByArcCode(arcCode).isPresent()) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR,
          String.format("档案编码重复: %s，请重试", arcCode));
    }

    return arcCode;
  }

  // 验证机构编码格式
  private void validateOrgCode(String orgCode, String orgType) {
    if (orgCode == null || orgCode.length() != 4) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR,
          String.format("%s编码格式错误: %s，应为4位数字", orgType, orgCode));
    }

    // 验证是否为数字
    try {
      Integer.parseInt(orgCode);
    } catch (NumberFormatException e) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR,
          String.format("%s编码包含非数字字符: %s", orgType, orgCode));
    }
  }

  // 获取字符串的后两位
  private String getLastTwoChars(String str) {
    if (str == null || str.length() < 2) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR,
          String.format("编码长度不足: %s", str));
    }
    return str.substring(str.length() - 2);
  }

  // 登记档案（人事专员提交）
  public Archive registerArchive(ArchiveDTO archiveDTO, Long userId) {
    log.info("开始登记档案，用户ID: {}", userId);
    log.info("档案DTO: {}", archiveDTO);

    // 数据验证
    validateArchive(archiveDTO);
    log.info("数据验证通过");

    // 创建档案实体
    Archive archive = new Archive();
    BeanUtils.copyProperties(archiveDTO, archive);

    // 设置登记人ID和提交时间
    archive.setWriteId(userId);
    archive.setSubmitTime(LocalDateTime.now());
    archive.setStatus(1); // 待复核
    log.info("设置基本属性完成");

    // 生成档案编码
    String arcCode = generateArcCode(archiveDTO.getThirdOrgId());
    archive.setArcCode(arcCode);
    log.info("生成档案编码: {}", arcCode);

    // 保存档案
    Archive savedArchive = archiveRepository.save(archive);

    // 记录操作日志（提交登记）
    recordArchiveOperation(savedArchive.getArcId(), userId, 1, "提交档案登记", null);

    log.info("人事专员 {} 提交档案登记，档案编码：{}", userId, arcCode);
    return savedArchive;
  }

  // 复核档案（人事经理审核）
  public Archive reviewArchive(ReviewDTO reviewDTO, Long reviewerId) {
    Archive archive = archiveRepository.findById(reviewDTO.getArcId())
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

    // 检查档案状态是否为待复核或重新提交待审核
    if (archive.getStatus() != 1 && archive.getStatus() != 4) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "该档案不在待复核状态");
    }

    // 记录变更字段（如果有修改）
    String changeFields = null;
    if (reviewDTO.getChangedData() != null && !reviewDTO.getChangedData().isEmpty()) {
      changeFields = convertMapToJson(reviewDTO.getChangedData());
    }

    // 更新档案状态
    if (reviewDTO.getAction() == 2) { // 审核通过
      archive.setStatus(2); // 已通过
      archive.setReviewId(reviewerId);
      archive.setReason(null); // 清除驳回原因

      // 自动生成用户账号
      try {
        User createdUser = createUserFromArchive(archive);
        log.info("档案审核通过，已自动创建用户账号: {}", createdUser.getUserCode());
      } catch (Exception e) {
        log.error("自动创建用户账号失败，档案ID: {}", archive.getArcId(), e);
      }

      // 记录操作日志（审核通过）
      recordArchiveOperation(archive.getArcId(), reviewerId, 2,
          "审核通过" + (StringUtils.hasText(reviewDTO.getComment()) ? "，备注：" + reviewDTO.getComment() : ""),
          changeFields);

      log.info("人事经理 {} 审核通过档案 {}，档案编码：{}", reviewerId, archive.getArcId(), archive.getArcCode());

    } else if (reviewDTO.getAction() == 3) { // 审核驳回
      archive.setStatus(3); // 已驳回
      archive.setReviewId(reviewerId);
      archive.setReason(reviewDTO.getComment()); // 保存驳回原因

      // 记录操作日志（审核驳回）
      recordArchiveOperation(archive.getArcId(), reviewerId, 3,
          "审核驳回，原因：" + reviewDTO.getComment(),
          changeFields);

      log.info("人事经理 {} 审核驳回档案 {}，原因：{}", reviewerId, archive.getArcId(), reviewDTO.getComment());
    } else {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "无效的操作类型");
    }

    return archiveRepository.save(archive);
  }

  // 重新提交档案（专员修改后重新提交）
  public Archive resubmitArchive(Long arcId, ArchiveDTO archiveDTO, Long userId) {
    Archive archive = archiveRepository.findById(arcId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

    // 检查档案状态是否为已驳回
    if (archive.getStatus() != 3) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有已驳回的档案可以重新提交");
    }

    // 验证数据
    validateArchive(archiveDTO);

    // 更新档案信息（保留原始档案编码和创建时间）
    BeanUtils.copyProperties(archiveDTO, archive, "arcId", "arcCode", "createTime");

    // 更新状态为重新提交待审核
    archive.setStatus(4);
    archive.setResubmitCount(archive.getResubmitCount() + 1);
    archive.setSubmitTime(LocalDateTime.now());

    Archive savedArchive = archiveRepository.save(archive);

    // 记录操作日志（重新提交）
    recordArchiveOperation(arcId, userId, 4, "重新提交档案审核", null);

    log.info("人事专员 {} 重新提交档案 {}，档案编码：{}", userId, arcId, archive.getArcCode());
    return savedArchive;
  }

  // 删除档案（标记为已删除）
  public Archive deleteArchive(Long arcId, Long userId) {
    Archive archive = archiveRepository.findById(arcId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

    // 检查档案状态
    if (archive.getStatus() == 1 || archive.getStatus() == 4) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "待复核状态的档案不能删除");
    }

    // 标记为已删除
    archive.setStatus(0);

    // 记录操作日志（删除）
    recordArchiveOperation(arcId, userId, 0, "删除档案", null);

    Archive savedArchive = archiveRepository.save(archive);

    // 重要：同步更新关联用户状态为离职
    updateUserStatusWhenArchiveDeleted(arcId);

    log.info("用户 {} 删除档案 {}，档案编码：{}，同步更新用户状态为离职",
        userId, arcId, archive.getArcCode());
    return savedArchive;
  }

  // 同步更新用户状态为离职（当档案被删除时）
  private void updateUserStatusWhenArchiveDeleted(Long arcId) {
    try {
      // 查找关联的用户
      Optional<User> userOptional = userRepository.findByArchiveId(arcId);

      if (userOptional.isPresent()) {
        User user = userOptional.get();

        // 只有当前状态为在职的用户才需要更新
        if (user.getStatus() != null && user.getStatus() == 1) {
          user.setStatus(0); // 离职
          user.setLeaveDate(LocalDate.now()); // 设置离职时间为今天
          user.setUpdateTime(LocalDateTime.now());

          userRepository.save(user);

          log.info("档案 {} 被删除，同步更新关联用户 {} (工号: {}) 状态为离职，离职时间: {}",
              arcId, user.getUserName(), user.getUserCode(), LocalDate.now());
        }
      } else {
        log.warn("档案 {} 没有关联的用户，跳过用户状态更新", arcId);
      }
    } catch (Exception e) {
      log.error("更新用户状态失败，档案ID: {}", arcId, e);
      // 这里不抛出异常，避免影响档案删除的主流程
    }
  }

  // 恢复已删除的档案
  public Archive restoreArchive(Long arcId, Long userId) {
    Archive archive = archiveRepository.findById(arcId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

    if (archive.getStatus() != 0) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有已删除的档案可以恢复");
    }

    // 恢复为正常状态（根据是否有复核人决定状态）
    if (archive.getReviewId() != null) {
      archive.setStatus(2); // 已通过
    } else {
      archive.setStatus(1); // 待复核
    }

    // 记录操作日志（恢复）
    recordArchiveOperation(arcId, userId, 5, "恢复档案", null);

    Archive savedArchive = archiveRepository.save(archive);

    // 重要：同步更新关联用户状态为在职
    updateUserStatusWhenArchiveRestored(arcId);

    log.info("用户 {} 恢复档案 {}，档案编码：{}，同步更新用户状态为在职",
        userId, arcId, archive.getArcCode());
    return savedArchive;
  }

  // 同步更新用户状态为在职（当档案被恢复时）
  private void updateUserStatusWhenArchiveRestored(Long arcId) {
    try {
      // 查找关联的用户
      Optional<User> userOptional = userRepository.findByArchiveId(arcId);

      if (userOptional.isPresent()) {
        User user = userOptional.get();

        // 只有当用户状态为离职时才需要恢复
        if (user.getStatus() != null && user.getStatus() == 0) {
          user.setStatus(1); // 在职
          user.setLeaveDate(null); // 清空离职时间
          user.setUpdateTime(LocalDateTime.now());

          userRepository.save(user);

          log.info("档案 {} 被恢复，同步更新关联用户 {} (工号: {}) 状态为在职，清空离职时间",
              arcId, user.getUserName(), user.getUserCode());
        }
      } else {
        log.warn("档案 {} 没有关联的用户，跳过用户状态更新", arcId);
      }
    } catch (Exception e) {
      log.error("更新用户状态失败，档案ID: {}", arcId, e);
      // 这里不抛出异常，避免影响档案恢复的主流程
    }
  }

  // 获取档案详情
  public Archive getArchiveDetail(Long arcId) {
    Archive archive = archiveRepository.findById(arcId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

    // 补充机构名称
    enrichArchiveWithOrgNames(archive);

    // 补充人员姓名
    enrichArchiveWithUserNames(archive);

    // 补充薪酬姓名
    enrichArchiveWithSalaryStandardName(archive);

    if (StringUtils.hasText(archive.getPositionName()) && archive.getThirdOrgId() != null) {
        // 根据三级机构 + 职位名称 反查职位 ID
        List<Position> posList = positionRepository.findByOrgIdAndPosName(
                archive.getThirdOrgId(), archive.getPositionName());
        if (!posList.isEmpty()) {
            archive.setPositionId(posList.get(0).getPosId());   // 只放内存，不保存 DB
        }
    }

    return archive;
  }

  // 分页查询档案
  public Page<Archive> queryArchives(ArchiveQueryDTO queryDTO, Pageable pageable) {
    Specification<Archive> spec = buildQuerySpecification(queryDTO);
    Page<Archive> page = archiveRepository.findAll(spec, pageable);

    // 补充关联信息
    page.getContent().forEach(archive -> {
      enrichArchiveWithOrgNames(archive);
      enrichArchiveWithUserNames(archive);
      enrichArchiveWithSalaryStandardName(archive);
    });

    return page;
  }

  // 获取待复核档案列表
  public List<Archive> getPendingReviewArchives() {
    List<Archive> archives = archiveRepository.findPendingReviewArchives();

    // 补充关联信息
    archives.forEach(archive -> {
      enrichArchiveWithOrgNames(archive);
      enrichArchiveWithUserNames(archive);
    });

    return archives;
  }

  // 获取档案审核流程
  public List<ArchiveOperation> getArchiveProcess(Long arcId) {
    List<ArchiveOperation> operations = archiveOperationRepository.findArchiveProcess(arcId);

    // 补充操作人姓名
    operations.forEach(op -> {
      User user = userRepository.findById(op.getOperatorId()).orElse(null);
      if (user != null) {
        op.setOperatorName(user.getUserName() != null ? user.getUserName() : user.getUserCode());
      }

      // 设置操作类型名称
      String[] typeNames = { "删除", "提交", "审核通过", "审核驳回", "重新提交", "恢复" };
      if (op.getOperationType() >= 0 && op.getOperationType() < typeNames.length) {
        op.setOperationTypeName(typeNames[op.getOperationType()]);
      }
    });

    return operations;
  }

  // 记录档案操作日志
  private void recordArchiveOperation(Long arcId, Long operatorId, Integer operationType,
      String comment, String changeFields) {
    ArchiveOperation operation = new ArchiveOperation();
    operation.setArcId(arcId);
    operation.setOperatorId(operatorId);
    operation.setOperationType(operationType);
    operation.setOperationComment(comment);
    operation.setChangeFields(changeFields);

    archiveOperationRepository.save(operation);
  }

  // 验证档案数据
  private void validateArchive(ArchiveDTO archiveDTO) {
    if (!StringUtils.hasText(archiveDTO.getName())) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "姓名不能为空");
    }

    if (archiveDTO.getSex() == null || (archiveDTO.getSex() != 1 && archiveDTO.getSex() != 2)) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "性别选择错误");
    }

    if (!StringUtils.hasText(archiveDTO.getIdCard()) || archiveDTO.getIdCard().length() != 18) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "身份证号必须为18位");
    }

    if (archiveDTO.getFirstOrgId() == null || archiveDTO.getSecondOrgId() == null ||
        archiveDTO.getThirdOrgId() == null) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "机构信息不完整");
    }

    if (!StringUtils.hasText(archiveDTO.getPositionName())) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "职位名称不能为空");
    }

    if (!StringUtils.hasText(archiveDTO.getTitle())) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "职称不能为空");
    }

    // 验证机构层级关系
    validateOrgHierarchy(archiveDTO.getFirstOrgId(), archiveDTO.getSecondOrgId(),
        archiveDTO.getThirdOrgId());
  }

  // 验证机构层级关系
  private void validateOrgHierarchy(Long firstOrgId, Long secondOrgId, Long thirdOrgId) {
    // 验证三级机构属于二级机构
    Organization thirdOrg = organizationRepository.findById(thirdOrgId)
        .orElseThrow(() -> new BusinessException(ResultCode.ORG_NOT_EXIST, "三级机构不存在"));

    // 使用longValue()进行比较，避免Long和Integer的equals问题
    if (thirdOrg.getParId() == null || thirdOrg.getParId().longValue() != secondOrgId.longValue()) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR,
          String.format("三级机构不属于指定的二级机构。三级机构ID=%d, 父级ID=%d, 期望父级ID=%d",
              thirdOrgId, thirdOrg.getParId(), secondOrgId));
    }

    // 验证二级机构属于一级机构
    Organization secondOrg = organizationRepository.findById(secondOrgId)
        .orElseThrow(() -> new BusinessException(ResultCode.ORG_NOT_EXIST, "二级机构不存在"));

    if (secondOrg.getParId() == null || secondOrg.getParId().longValue() != firstOrgId.longValue()) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR,
          String.format("二级机构不属于指定的一级机构。二级机构ID=%d, 父级ID=%d, 期望父级ID=%d",
              secondOrgId, secondOrg.getParId(), firstOrgId));
    }
  }

  // 构建查询条件
  private Specification<Archive> buildQuerySpecification(ArchiveQueryDTO queryDTO) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 按档案状态查询
      if (queryDTO.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
      }

      // 按登记人查询
      if (queryDTO.getWriteId() != null) {
        predicates.add(cb.equal(root.get("writeId"), queryDTO.getWriteId()));
      }

      // 按复核人查询
      if (queryDTO.getReviewId() != null) {
        predicates.add(cb.equal(root.get("reviewId"), queryDTO.getReviewId()));
      }

      // 按三级机构查询
      if (queryDTO.getThirdOrgId() != null) {
        predicates.add(cb.equal(root.get("thirdOrgId"), queryDTO.getThirdOrgId()));
      }

      // 按姓名模糊查询
      if (StringUtils.hasText(queryDTO.getName())) {
        predicates.add(cb.like(root.get("name"), "%" + queryDTO.getName() + "%"));
      }

      // 按身份证号查询
      if (StringUtils.hasText(queryDTO.getIdCard())) {
        predicates.add(cb.like(root.get("idCard"), "%" + queryDTO.getIdCard() + "%"));
      }

      // 按创建时间范围查询
      if (queryDTO.getCreateTimeStart() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), queryDTO.getCreateTimeStart()));
      }

      if (queryDTO.getCreateTimeEnd() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), queryDTO.getCreateTimeEnd()));
      }

      // 如果明确查询状态为0的档案，就不应该排除
      if (queryDTO.getStatus() == null) {
        // 只有未指定状态时才排除已删除的档案
        predicates.add(cb.notEqual(root.get("status"), 0));
      }
      // 如果queryDTO.getStatus() == 0，则查询已删除的档案
      // 如果queryDTO.getStatus() != 0，则查询指定状态的档案

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  // 补充机构名称
  public void enrichArchiveWithOrgNames(Archive archive) {
    try {
      Organization firstOrg = organizationRepository.findById(archive.getFirstOrgId()).orElse(null);
      if (firstOrg != null) {
        archive.setFirstOrgName(firstOrg.getOrgName());
      }

      Organization secondOrg = organizationRepository.findById(archive.getSecondOrgId()).orElse(null);
      if (secondOrg != null) {
        archive.setSecondOrgName(secondOrg.getOrgName());
      }

      Organization thirdOrg = organizationRepository.findById(archive.getThirdOrgId()).orElse(null);
      if (thirdOrg != null) {
        archive.setThirdOrgName(thirdOrg.getOrgName());
      }
    } catch (Exception e) {
      log.error("获取机构名称失败", e);
    }
  }

  // 补充人员姓名
  public void enrichArchiveWithUserNames(Archive archive) {
    try {
      User writer = userRepository.findById(archive.getWriteId()).orElse(null);
      if (writer != null) {
        // 使用 getUserName() 而不是 getRealName()
        archive.setWriterName(writer.getUserName() != null ? writer.getUserName() : writer.getUserCode());
      }

      if (archive.getReviewId() != null) {
        User reviewer = userRepository.findById(archive.getReviewId()).orElse(null);
        if (reviewer != null) {
          // 使用 getUserName() 而不是 getRealName()
          archive.setReviewerName(reviewer.getUserName() != null ? reviewer.getUserName() : reviewer.getUserCode());
        }
      }
    } catch (Exception e) {
      log.error("获取用户姓名失败", e);
    }
  }

  // 补充薪酬标准名称
  private void enrichArchiveWithSalaryStandardName(Archive archive) {
    try {
      if (archive.getSalaryStandard() != null) {
        Optional<ArchiveStandard> standardOptional = archiveStandardRepository.findById(archive.getSalaryStandard());
        if (standardOptional.isPresent()) {
          ArchiveStandard standard = standardOptional.get();
          archive.setSalaryStandardName(standard.getStandardName());
        }
      }
    } catch (Exception e) {
      log.error("获取薪酬标准名称失败", e);
    }
  }

  // 转换Map为JSON字符串
  private String convertMapToJson(Map<String, Object> map) {
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      return mapper.writeValueAsString(map);
    } catch (Exception e) {
      log.error("转换JSON失败", e);
      return "{}";
    }
  }

  // 统计各类档案数量
  public Map<String, Long> getArchiveStats() {
    Map<String, Long> stats = new HashMap<>();

    // 待复核
    stats.put("pending", archiveRepository.count((root, query, cb) -> cb.and(cb.equal(root.get("status"), 1))));

    // 已通过
    stats.put("approved", archiveRepository.count((root, query, cb) -> cb.and(cb.equal(root.get("status"), 2))));

    // 已驳回
    stats.put("rejected", archiveRepository.count((root, query, cb) -> cb.and(cb.equal(root.get("status"), 3))));

    // 重新提交待审核
    stats.put("resubmitted", archiveRepository.count((root, query, cb) -> cb.and(cb.equal(root.get("status"), 4))));

    // 总数（排除已删除）
    stats.put("total", archiveRepository.count((root, query, cb) -> cb.notEqual(root.get("status"), 0)));

    return stats;
  }

  // 检查档案是否存在
  public boolean existsByArcCode(String arcCode) {
    return archiveRepository.findByArcCode(arcCode).isPresent();
  }

  // 根据用户ID获取其登记的档案
  public List<Archive> getArchivesByWriter(Long writerId) {
    List<Archive> archives = archiveRepository.findByWriteId(writerId);

    // 补充关联信息
    archives.forEach(archive -> {
      enrichArchiveWithOrgNames(archive);
      enrichArchiveWithUserNames(archive);
    });
    return archives;
  }

  // 自动生成用户账号
  private User createUserFromArchive(Archive archive) {
    log.info("开始为档案 {} 创建用户账号", archive.getArcId());

    try {
      Long posId = null;
      List<Position> list = positionRepository.findByOrgIdAndPosName(
              archive.getThirdOrgId(), archive.getPositionName());
      if (list != null && !list.isEmpty()) {
          posId = list.get(0).getPosId();
      }
      if (posId == null) {
          throw new BusinessException(ResultCode.BUSINESS_ERROR,
                  "无法找到三级机构[" + archive.getThirdOrgId() + "]下职位[" + archive.getPositionName() + "]，请先维护职位设置");
      }

      // 检查是否已存在相同档案ID的用户
      Optional<User> existingUser = userRepository.findByArchiveId(archive.getArcId());
      if (existingUser.isPresent()) {
        log.warn("档案 {} 已存在对应的用户账号，跳过创建", archive.getArcId());
        return existingUser.get();
      }

      // 生成工号：HRM + 6位顺序号
      String userCode = generateUserCode();
      log.info("生成的工号: {}", userCode);

      // 创建用户对象
      User user = new User();
      user.setUserCode(userCode);
      user.setUserName(archive.getName());
      user.setEmail(archive.getEmail());
      user.setPhone(archive.getPhone());
      user.setOrgId(archive.getThirdOrgId()); // 使用三级机构ID
      user.setOrgId(archive.getThirdOrgId());
      user.setPosId(posId); 
      user.setEntryDate(LocalDate.now()); // 入职时间就是建档成功时间
      user.setStatus(1); // 在职状态

      // 初始密码为身份证后6位
      String idCard = archive.getIdCard();
      String initialPassword = idCard.substring(Math.max(0, idCard.length() - 6));
      user.setPassword(passwordEncoder.encode(initialPassword));

      // 设置默认角色类型为6（普通员工）
      user.setRoleType(6);

      // 关联档案ID
      user.setArchiveId(archive.getArcId());

      // 设置其他信息
      user.setCreateTime(LocalDateTime.now());
      user.setUpdateTime(LocalDateTime.now());

      User savedUser = userRepository.save(user);

      log.info("成功创建用户账号: {}, 工号: {}, 初始密码: {}",
          savedUser.getUserName(), savedUser.getUserCode(), initialPassword);

      return savedUser;
    } catch (Exception e) {
      log.error("创建用户账号失败，档案ID: {}", archive.getArcId(), e);
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "创建用户账号失败: " + e.getMessage());
    }
  }

  // 生成用户工号
  private String generateUserCode() {
    // 查询最大工号
    String maxUserCode = userRepository.findMaxUserCode();
    int nextNum = 1;

    if (maxUserCode != null && maxUserCode.startsWith("HRM")) {
      try {
        String numStr = maxUserCode.substring(3);
        nextNum = Integer.parseInt(numStr) + 1;
      } catch (NumberFormatException e) {
        log.warn("解析工号失败，重置为1", e);
        nextNum = 1;
      }
    }

    return String.format("HRM%06d", nextNum);
  }

  public Page<Archive> queryMyArchives(Map<String, Object> params, int page, int size) {
    // 构建分页
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

    // 构建查询条件
    Specification<Archive> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 必须是指定登记人的档案
      if (params.get("writeId") != null) {
        predicates.add(cb.equal(root.get("writeId"), params.get("writeId")));
      }

      // 档案编号模糊查询
      if (params.get("arcCode") != null) {
        predicates.add(cb.like(root.get("arcCode"), "%" + params.get("arcCode") + "%"));
      }

      // 姓名模糊查询
      if (params.get("name") != null) {
        predicates.add(cb.like(root.get("name"), "%" + params.get("name") + "%"));
      }

      // 职位模糊查询
      if (params.get("positionName") != null) {
        predicates.add(cb.like(root.get("positionName"), "%" + params.get("positionName") + "%"));
      }

      // 状态查询
      if (params.get("status") != null) {
        predicates.add(cb.equal(root.get("status"), params.get("status")));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    return archiveRepository.findAll(spec, pageable);
  }

  // 获取薪酬标准
  public List<Map<String, Object>> getSalaryStandardsByPositionAndTitle(Long positionId, String title) {
    try {
        List<ArchiveStandard> standards = archiveSalaryService.getStandardsByPositionAndTitle(positionId, title);
        
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
    } catch (Exception e) {
        log.error("获取薪酬标准失败", e);
        return new ArrayList<>();
    }
}
}
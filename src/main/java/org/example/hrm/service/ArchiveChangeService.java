package org.example.hrm.service;

import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.ArchiveChangeDTO;
import org.example.hrm.dto.ArchiveChangeQueryDTO;
import org.example.hrm.dto.ReviewChangeDTO;
import org.example.hrm.entity.*;
import org.example.hrm.exception.BusinessException;
import org.example.hrm.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.criteria.Predicate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ArchiveChangeService {

  @Autowired
  private ArchiveChangeRepository archiveChangeRepository;

  @Autowired
  private ChangeOperationRepository changeOperationRepository;

  @Autowired
  private ArchiveRepository archiveRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ObjectMapper objectMapper;

  // 生成变更编码
  private String generateChangeCode() {
    String year = String.valueOf(Year.now().getValue());

    // 获取今年的变更数量
    Long count = archiveChangeRepository.countByYear();
    if (count == null) {
      count = 0L;
    }

    // 生成顺序号（4位）
    String sequence = String.format("%04d", count + 1);
    String changeCode = year + sequence;

    // 验证编码是否唯一
    if (archiveChangeRepository.findByChangeCode(changeCode).isPresent()) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "变更编码重复，请重试");
    }

    return changeCode;
  }

  // 申请档案变更（人事专员）
  public ArchiveChange applyArchiveChange(ArchiveChangeDTO changeDTO, Long userId) {
    log.info("开始申请档案变更，用户ID: {}", userId);

    // 验证数据
    validateChangeDTO(changeDTO);

    // 获取原始档案
    Archive originalArchive = archiveRepository.findById(changeDTO.getArcId())
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

    // 检查档案状态，只有已通过的档案才能变更
    if (originalArchive.getStatus() != 2) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有已通过的档案才能申请变更");
    }

    // 检查该档案是否有待复核的变更申请
    List<Integer> pendingStatuses = Arrays.asList(1, 4);
    List<ArchiveChange> pendingChanges = archiveChangeRepository.findByArcIdAndStatusIn(originalArchive.getArcId(),
        pendingStatuses);
    if (pendingChanges != null && !pendingChanges.isEmpty()) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "该档案有待审核的变更申请，不能重复提交");
    }

    // 创建变更申请
    ArchiveChange archiveChange = new ArchiveChange();
    archiveChange.setArcId(changeDTO.getArcId());
    archiveChange.setChangeReason(changeDTO.getChangeReason());
    archiveChange.setApplyId(userId);
    archiveChange.setStatus(1); // 待复核

    // 生成变更编码
    String changeCode = generateChangeCode();
    archiveChange.setChangeCode(changeCode);

    try {
      // 使用对象转JSON的方式获取变更前的完整数据
      String beforeDataJson = convertArchiveToJson(originalArchive);
      archiveChange.setBeforeData(beforeDataJson);
      log.info("成功保存变更前数据，长度: {}", beforeDataJson.length());

      // 创建变更后的数据副本
      Archive afterArchive = new Archive();
      BeanUtils.copyProperties(originalArchive, afterArchive);

      // 更新变更后的字段
      updateArchiveFromDTO(afterArchive, changeDTO);

      // 将变更后的数据转换为JSON
      String afterDataJson = convertArchiveToJson(afterArchive);
      archiveChange.setAfterData(afterDataJson);

      // 比较并记录变更的字段
      List<String> changedFields = compareArchives(originalArchive, afterArchive);
      archiveChange.setChangedFields(objectMapper.writeValueAsString(changedFields));

    } catch (Exception e) {
      log.error("处理变更数据失败", e);
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "处理变更数据失败: " + e.getMessage());
    }

    // 保存变更申请
    ArchiveChange savedChange = archiveChangeRepository.save(archiveChange);

    // 记录操作日志
    recordChangeOperation(savedChange.getChangeId(), userId, 1,
        "提交档案变更申请，原因：" + changeDTO.getChangeReason());

    log.info("人事专员 {} 提交档案变更申请，变更编码：{}", userId, changeCode);
    return savedChange;
  }

  // 将Archive对象转换为JSON字符串
  private String convertArchiveToJson(Archive archive) throws Exception {
    // 先复制对象，避免修改原始对象
    Archive copy = new Archive();
    BeanUtils.copyProperties(archive, copy);

    // 将对象转换为Map，处理LocalDate等特殊类型
    Map<String, Object> dataMap = new HashMap<>();

    // 手动转换每个字段
    dataMap.put("arcId", copy.getArcId());
    dataMap.put("arcCode", copy.getArcCode());
    dataMap.put("name", copy.getName());
    dataMap.put("sex", copy.getSex());
    dataMap.put("idCard", copy.getIdCard());
    dataMap.put("firstOrgId", copy.getFirstOrgId());
    dataMap.put("secondOrgId", copy.getSecondOrgId());
    dataMap.put("thirdOrgId", copy.getThirdOrgId());
    dataMap.put("positionName", copy.getPositionName());
    dataMap.put("title", copy.getTitle());
    dataMap.put("salaryStandard", copy.getSalaryStandard());

    // 处理LocalDate类型
    if (copy.getBirDate() != null) {
      dataMap.put("birDate", copy.getBirDate().toString());
    } else {
      dataMap.put("birDate", null);
    }

    dataMap.put("nationality", copy.getNationality());
    dataMap.put("qualification", copy.getQualification());
    dataMap.put("email", copy.getEmail());
    dataMap.put("telephone", copy.getTelephone());
    dataMap.put("phone", copy.getPhone());
    dataMap.put("address", copy.getAddress());
    dataMap.put("zipCode", copy.getZipCode());
    dataMap.put("country", copy.getCountry());
    dataMap.put("birAddress", copy.getBirAddress());
    dataMap.put("belief", copy.getBelief());
    dataMap.put("identity", copy.getIdentity());
    dataMap.put("major", copy.getMajor());
    dataMap.put("photoPath", copy.getPhotoPath());
    dataMap.put("writeId", copy.getWriteId());
    dataMap.put("status", copy.getStatus());
    dataMap.put("reviewId", copy.getReviewId());
    dataMap.put("reason", copy.getReason());
    dataMap.put("resubmitCount", copy.getResubmitCount());

    // 处理LocalDateTime类型
    if (copy.getCreateTime() != null) {
      dataMap.put("createTime", copy.getCreateTime().toString());
    } else {
      dataMap.put("createTime", null);
    }

    if (copy.getSubmitTime() != null) {
      dataMap.put("submitTime", copy.getSubmitTime().toString());
    } else {
      dataMap.put("submitTime", null);
    }

    // 转换为JSON字符串
    return objectMapper.writeValueAsString(dataMap);
  }

  // 从DTO更新Archive对象
  private void updateArchiveFromDTO(Archive archive, ArchiveChangeDTO changeDTO) {
    if (StringUtils.hasText(changeDTO.getName())) {
      archive.setName(changeDTO.getName());
    }
    if (changeDTO.getSex() != null) {
      archive.setSex(changeDTO.getSex());
    }
    if (StringUtils.hasText(changeDTO.getIdCard())) {
      archive.setIdCard(changeDTO.getIdCard());
    }
    if (StringUtils.hasText(changeDTO.getTitle())) {
      archive.setTitle(changeDTO.getTitle());
    }
    if (changeDTO.getSalaryStandard() != null) {
      archive.setSalaryStandard(changeDTO.getSalaryStandard());
    }
    if (changeDTO.getBirDate() != null) {
      archive.setBirDate(changeDTO.getBirDate());
    }
    if (StringUtils.hasText(changeDTO.getNationality())) {
      archive.setNationality(changeDTO.getNationality());
    }
    if (StringUtils.hasText(changeDTO.getQualification())) {
      archive.setQualification(changeDTO.getQualification());
    }
    if (StringUtils.hasText(changeDTO.getEmail())) {
      archive.setEmail(changeDTO.getEmail());
    }
    if (StringUtils.hasText(changeDTO.getTelephone())) {
      archive.setTelephone(changeDTO.getTelephone());
    }
    if (StringUtils.hasText(changeDTO.getPhone())) {
      archive.setPhone(changeDTO.getPhone());
    }
    if (StringUtils.hasText(changeDTO.getAddress())) {
      archive.setAddress(changeDTO.getAddress());
    }
    if (StringUtils.hasText(changeDTO.getZipCode())) {
      archive.setZipCode(changeDTO.getZipCode());
    }
    if (StringUtils.hasText(changeDTO.getCountry())) {
      archive.setCountry(changeDTO.getCountry());
    }
    if (StringUtils.hasText(changeDTO.getBirAddress())) {
      archive.setBirAddress(changeDTO.getBirAddress());
    }
    if (StringUtils.hasText(changeDTO.getBelief())) {
      archive.setBelief(changeDTO.getBelief());
    }
    if (StringUtils.hasText(changeDTO.getIdentity())) {
      archive.setIdentity(changeDTO.getIdentity());
    }
    if (StringUtils.hasText(changeDTO.getMajor())) {
      archive.setMajor(changeDTO.getMajor());
    }
    if (StringUtils.hasText(changeDTO.getPhotoPath())) {
      archive.setPhotoPath(changeDTO.getPhotoPath());
    }
  }

  // 比较两个Archive对象，找出变更的字段
  private List<String> compareArchives(Archive before, Archive after) {
    List<String> changedFields = new ArrayList<>();

    // 使用反射比较所有字段
    java.lang.reflect.Field[] fields = Archive.class.getDeclaredFields();

    for (java.lang.reflect.Field field : fields) {
      try {
        field.setAccessible(true);
        Object beforeValue = field.get(before);
        Object afterValue = field.get(after);

        // 比较值是否相等
        if (beforeValue == null && afterValue != null) {
          changedFields.add(field.getName());
        } else if (beforeValue != null && !beforeValue.equals(afterValue)) {
          changedFields.add(field.getName());
        }
      } catch (IllegalAccessException e) {
        log.warn("无法访问字段: {}", field.getName(), e);
      }
    }

    return changedFields;
  }

  // 审核变更申请（人事经理）
  public ArchiveChange reviewChange(ReviewChangeDTO reviewDTO, Long reviewerId) {
    ArchiveChange archiveChange = archiveChangeRepository.findById(reviewDTO.getChangeId())
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "变更申请不存在"));

    // 检查状态是否为待复核
    if (archiveChange.getStatus() != 1) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "该变更申请不在待复核状态");
    }

    // 获取原始档案
    Archive originalArchive = archiveRepository.findById(archiveChange.getArcId())
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

    if (reviewDTO.getAction() == 2) { // 审核通过
      try {
        // 解析变更后的数据
        Map<String, Object> afterData = objectMapper.readValue(
            archiveChange.getAfterData(), Map.class);

        // 如果经理有修改，更新数据
        if (reviewDTO.getChangedData() != null && !reviewDTO.getChangedData().isEmpty()) {
          afterData.putAll(reviewDTO.getChangedData());
          archiveChange.setAfterData(objectMapper.writeValueAsString(afterData));

          // 重新计算变更字段
          Map<String, Object> beforeData = objectMapper.readValue(
              archiveChange.getBeforeData(), Map.class);
          List<String> changedFields = compareData(beforeData, afterData);
          archiveChange.setChangedFields(objectMapper.writeValueAsString(changedFields));
        }

        // 更新档案信息
        updateArchiveFromMap(originalArchive, afterData);
        archiveRepository.save(originalArchive);

        // 更新变更申请状态
        archiveChange.setStatus(2); // 已通过
        archiveChange.setReviewId(reviewerId);
        archiveChange.setReviewComment(reviewDTO.getReviewComment());

        // 记录操作日志
        recordChangeOperation(archiveChange.getChangeId(), reviewerId, 2,
            "审核通过" + (StringUtils.hasText(reviewDTO.getReviewComment()) ? "，备注：" + reviewDTO.getReviewComment() : ""));

        log.info("人事经理 {} 审核通过变更申请 {}，变更编码：{}",
            reviewerId, archiveChange.getChangeId(), archiveChange.getChangeCode());

      } catch (Exception e) {
        log.error("审核通过时处理数据失败", e);
        throw new BusinessException(ResultCode.BUSINESS_ERROR, "处理数据失败");
      }

    } else if (reviewDTO.getAction() == 3) { // 审核驳回
      archiveChange.setStatus(3); // 已驳回
      archiveChange.setReviewId(reviewerId);
      archiveChange.setReviewComment(reviewDTO.getReviewComment());

      // 记录操作日志
      recordChangeOperation(archiveChange.getChangeId(), reviewerId, 3,
          "审核驳回，原因：" + reviewDTO.getReviewComment());

      log.info("人事经理 {} 审核驳回变更申请 {}，原因：{}",
          reviewerId, archiveChange.getChangeId(), reviewDTO.getReviewComment());
    } else {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "无效的操作类型");
    }

    return archiveChangeRepository.save(archiveChange);
  }

  // 重新提交变更申请（人事专员）
  public ArchiveChange resubmitChange(Long changeId, ArchiveChangeDTO changeDTO, Long userId) {
    ArchiveChange archiveChange = archiveChangeRepository.findById(changeId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "变更申请不存在"));

    // 检查状态是否为已驳回
    if (archiveChange.getStatus() != 3) {
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有已驳回的变更申请可以重新提交");
    }

    // 更新变更原因
    archiveChange.setChangeReason(changeDTO.getChangeReason());

    try {
      // 获取原始档案
      Archive originalArchive = archiveRepository.findById(archiveChange.getArcId())
          .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "档案不存在"));

      Map<String, Object> beforeData;

      // 读取之前保存的 beforeData（这是原始档案数据）
      if (archiveChange.getBeforeData() != null) {
        beforeData = objectMapper.readValue(archiveChange.getBeforeData(),
            new TypeReference<Map<String, Object>>() {
            });
        log.info("重新提交变更申请，使用之前保存的beforeData");
      } else {
        // 如果没有保存的beforeData，则从档案获取
        beforeData = getArchiveDataMap(originalArchive);
        log.info("重新提交变更申请，从档案获取beforeData");
      }

      archiveChange.setBeforeData(objectMapper.writeValueAsString(beforeData));
      log.info("重新提交变更申请，变更前数据: {}", beforeData);

      // 创建变更后的数据副本，基于原始档案
      Map<String, Object> afterData = new HashMap<>();
      BeanUtils.copyProperties(originalArchive, afterData);

      // 更新变更后的字段（使用changeDTO中的数据）
      updateChangedFieldsForResubmit(afterData, changeDTO);
      log.info("重新提交变更申请，变更后数据: {}", afterData);
      archiveChange.setAfterData(objectMapper.writeValueAsString(afterData));

      // 记录变更的字段（比较beforeData和afterData）
      List<String> changedFields = compareData(beforeData, afterData);
      log.info("重新提交变更申请，变更字段: {}", changedFields);
      archiveChange.setChangedFields(objectMapper.writeValueAsString(changedFields));

    } catch (Exception e) {
      log.error("重新提交时处理数据失败", e);
      throw new BusinessException(ResultCode.BUSINESS_ERROR, "处理数据失败: " + e.getMessage());
    }

    archiveChange.setStatus(4); 

    // 记录操作日志
    recordChangeOperation(changeId, userId, 4,
        "重新提交变更申请，原因：" + changeDTO.getChangeReason());

    log.info("人事专员 {} 重新提交变更申请 {}，变更编码：{}",
        userId, changeId, archiveChange.getChangeCode());

    return archiveChangeRepository.save(archiveChange);
  }

  // 针对重新提交的字段更新方法
  private void updateChangedFieldsForResubmit(Map<String, Object> data, ArchiveChangeDTO changeDTO) {
    // 只更新DTO中提供的非空字段
    if (StringUtils.hasText(changeDTO.getName())) {
      data.put("name", changeDTO.getName());
    }
    if (changeDTO.getSex() != null) {
      data.put("sex", changeDTO.getSex());
    }
    if (StringUtils.hasText(changeDTO.getIdCard())) {
      data.put("idCard", changeDTO.getIdCard());
    }
    if (StringUtils.hasText(changeDTO.getTitle())) {
      data.put("title", changeDTO.getTitle());
    }
    if (changeDTO.getSalaryStandard() != null) {
      data.put("salaryStandard", changeDTO.getSalaryStandard());
    }
    if (changeDTO.getBirDate() != null) {
      data.put("birDate", changeDTO.getBirDate().toString());
    }
    if (StringUtils.hasText(changeDTO.getNationality())) {
      data.put("nationality", changeDTO.getNationality());
    }
    if (StringUtils.hasText(changeDTO.getQualification())) {
      data.put("qualification", changeDTO.getQualification());
    }
    if (StringUtils.hasText(changeDTO.getEmail())) {
      data.put("email", changeDTO.getEmail());
    }
    if (StringUtils.hasText(changeDTO.getTelephone())) {
      data.put("telephone", changeDTO.getTelephone());
    }
    if (StringUtils.hasText(changeDTO.getPhone())) {
      data.put("phone", changeDTO.getPhone());
    }
    if (StringUtils.hasText(changeDTO.getAddress())) {
      data.put("address", changeDTO.getAddress());
    }
    if (StringUtils.hasText(changeDTO.getZipCode())) {
      data.put("zipCode", changeDTO.getZipCode());
    }
    if (StringUtils.hasText(changeDTO.getCountry())) {
      data.put("country", changeDTO.getCountry());
    }
    if (StringUtils.hasText(changeDTO.getBirAddress())) {
      data.put("birAddress", changeDTO.getBirAddress());
    }
    if (StringUtils.hasText(changeDTO.getBelief())) {
      data.put("belief", changeDTO.getBelief());
    }
    if (StringUtils.hasText(changeDTO.getIdentity())) {
      data.put("identity", changeDTO.getIdentity());
    }
    if (StringUtils.hasText(changeDTO.getMajor())) {
      data.put("major", changeDTO.getMajor());
    }
    if (StringUtils.hasText(changeDTO.getPhotoPath())) {
      data.put("photoPath", changeDTO.getPhotoPath());
    }
  }

  // 检查档案是否有待审核的变更申请
  public boolean hasPendingChange(Long arcId) {
    // 状态1: 待复核, 状态4: 重新提交待审核
    List<Integer> pendingStatuses = Arrays.asList(1, 4);
    List<ArchiveChange> pendingChanges = archiveChangeRepository.findByArcIdAndStatusIn(arcId, pendingStatuses);
    return pendingChanges != null && !pendingChanges.isEmpty();
  }

  // 获取变更申请详情
  public ArchiveChange getChangeDetail(Long changeId) {
    ArchiveChange archiveChange = archiveChangeRepository.findById(changeId)
        .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_EXIST, "变更申请不存在"));

    // 补充关联信息
    enrichChangeWithRelatedInfo(archiveChange);

    return archiveChange;
  }

  // 分页查询变更申请
  public Page<ArchiveChange> queryChanges(ArchiveChangeQueryDTO queryDTO, Pageable pageable) {
    Specification<ArchiveChange> spec = buildQuerySpecification(queryDTO);
    Page<ArchiveChange> page = archiveChangeRepository.findAll(spec, pageable);

    // 补充关联信息
    page.getContent().forEach(this::enrichChangeWithRelatedInfo);

    return page;
  }

  // 获取待复核变更申请列表
  public List<ArchiveChange> getPendingReviewChanges() {
    List<ArchiveChange> changes = archiveChangeRepository.findByStatusIn(Arrays.asList(1, 4));
    changes.forEach(this::enrichChangeWithRelatedInfo);
    return changes;
  }

  // 获取变更审核流程
  public List<ChangeOperation> getChangeProcess(Long changeId) {
    List<ChangeOperation> operations = changeOperationRepository.findChangeProcess(changeId);

    // 补充操作人姓名
    operations.forEach(op -> {
      User user = userRepository.findById(op.getOperatorId()).orElse(null);
      if (user != null) {
        op.setOperatorName(user.getUserName() != null ? user.getUserName() : user.getUserCode());
      }

      // 设置操作类型名称
      String[] typeNames = { "", "提交", "审核通过", "审核驳回", "重新提交" };
      if (op.getOperationType() >= 1 && op.getOperationType() < typeNames.length) {
        op.setOperationTypeName(typeNames[op.getOperationType()]);
      }
    });

    return operations;
  }

  // 根据档案ID获取变更历史
  public List<ArchiveChange> getChangeHistoryByArchive(Long arcId) {
    List<ArchiveChange> changes = archiveChangeRepository.findByArcId(arcId);
    changes.forEach(this::enrichChangeWithRelatedInfo);
    return changes;
  }

  // 记录变更操作日志
  private void recordChangeOperation(Long changeId, Long operatorId, Integer operationType,
      String comment) {
    ChangeOperation operation = new ChangeOperation();
    operation.setChangeId(changeId);
    operation.setOperatorId(operatorId);
    operation.setOperationType(operationType);
    operation.setOperationComment(comment);

    changeOperationRepository.save(operation);
  }

  // 验证变更DTO
  private void validateChangeDTO(ArchiveChangeDTO changeDTO) {
    if (changeDTO.getArcId() == null) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "档案ID不能为空");
    }

    if (!StringUtils.hasText(changeDTO.getChangeReason())) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "变更原因不能为空");
    }

    // 验证身份证号（如果提供）
    if (StringUtils.hasText(changeDTO.getIdCard()) && changeDTO.getIdCard().length() != 18) {
      throw new BusinessException(ResultCode.VALIDATE_FAILED, "身份证号必须为18位");
    }
  }

  // 将档案对象转换为Map
  private Map<String, Object> getArchiveDataMap(Archive archive) {
    Map<String, Object> data = new HashMap<>();
    BeanUtils.copyProperties(archive, data);
    return data;
  }

  // 更新变更字段
  // private void updateChangedFields(Map<String, Object> data, ArchiveChangeDTO changeDTO) {
  //   // 只更新DTO中提供的字段
  //   if (StringUtils.hasText(changeDTO.getName())) {
  //     data.put("name", changeDTO.getName());
  //   }
  //   if (changeDTO.getSex() != null) {
  //     data.put("sex", changeDTO.getSex());
  //   }
  //   if (StringUtils.hasText(changeDTO.getIdCard())) {
  //     data.put("idCard", changeDTO.getIdCard());
  //   }
  //   if (StringUtils.hasText(changeDTO.getTitle())) {
  //     data.put("title", changeDTO.getTitle());
  //   }
  //   if (changeDTO.getSalaryStandard() != null) {
  //     data.put("salaryStandard", changeDTO.getSalaryStandard());
  //   }
  //   if (changeDTO.getBirDate() != null) {
  //     data.put("birDate", changeDTO.getBirDate());
  //   }
  //   if (StringUtils.hasText(changeDTO.getNationality())) {
  //     data.put("nationality", changeDTO.getNationality());
  //   }
  //   if (StringUtils.hasText(changeDTO.getQualification())) {
  //     data.put("qualification", changeDTO.getQualification());
  //   }
  //   if (StringUtils.hasText(changeDTO.getEmail())) {
  //     data.put("email", changeDTO.getEmail());
  //   }
  //   if (StringUtils.hasText(changeDTO.getTelephone())) {
  //     data.put("telephone", changeDTO.getTelephone());
  //   }
  //   if (StringUtils.hasText(changeDTO.getPhone())) {
  //     data.put("phone", changeDTO.getPhone());
  //   }
  //   if (StringUtils.hasText(changeDTO.getAddress())) {
  //     data.put("address", changeDTO.getAddress());
  //   }
  //   if (StringUtils.hasText(changeDTO.getZipCode())) {
  //     data.put("zipCode", changeDTO.getZipCode());
  //   }
  //   if (StringUtils.hasText(changeDTO.getCountry())) {
  //     data.put("country", changeDTO.getCountry());
  //   }
  //   if (StringUtils.hasText(changeDTO.getBirAddress())) {
  //     data.put("birAddress", changeDTO.getBirAddress());
  //   }
  //   if (StringUtils.hasText(changeDTO.getBelief())) {
  //     data.put("belief", changeDTO.getBelief());
  //   }
  //   if (StringUtils.hasText(changeDTO.getIdentity())) {
  //     data.put("identity", changeDTO.getIdentity());
  //   }
  //   if (StringUtils.hasText(changeDTO.getMajor())) {
  //     data.put("major", changeDTO.getMajor());
  //   }
  //   if (StringUtils.hasText(changeDTO.getPhotoPath())) {
  //     data.put("photoPath", changeDTO.getPhotoPath());
  //   }
  // }

  // 比较数据，找出变更的字段
  private List<String> compareData(Map<String, Object> beforeData, Map<String, Object> afterData) {
    List<String> changedFields = new ArrayList<>();

    for (Map.Entry<String, Object> entry : afterData.entrySet()) {
      String key = entry.getKey();
      Object afterValue = entry.getValue();
      Object beforeValue = beforeData.get(key);

      // 比较值是否相等
      if (beforeValue == null && afterValue != null) {
        changedFields.add(key);
      } else if (beforeValue != null && !beforeValue.equals(afterValue)) {
        changedFields.add(key);
      }
    }

    return changedFields;
  }

  // 根据Map更新档案
  private void updateArchiveFromMap(Archive archive, Map<String, Object> data) {
    // 修复日期类型转换
    if (data.containsKey("birDate")) {
      Object birDateObj = data.get("birDate");
      if (birDateObj instanceof String) {
        try {
          archive.setBirDate(java.time.LocalDate.parse((String) birDateObj));
        } catch (Exception e) {
          log.error("日期格式转换失败: {}", birDateObj, e);
          // 尝试其他格式
          try {
            archive.setBirDate(java.time.LocalDate.parse((String) birDateObj,
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
          } catch (Exception ex) {
            log.error("日期解析失败，使用原始值: {}", birDateObj);
          }
        }
      } else if (birDateObj instanceof java.time.LocalDate) {
        archive.setBirDate((java.time.LocalDate) birDateObj);
      }
    }

    // 其他字段保持不变
    if (data.containsKey("name")) {
      archive.setName((String) data.get("name"));
    }
    if (data.containsKey("sex")) {
      Object sexObj = data.get("sex");
      if (sexObj instanceof Number) {
        archive.setSex(((Number) sexObj).intValue());
      } else if (sexObj instanceof String) {
        archive.setSex(Integer.parseInt((String) sexObj));
      } else {
        archive.setSex((Integer) sexObj);
      }
    }
    if (data.containsKey("idCard")) {
      archive.setIdCard((String) data.get("idCard"));
    }
    if (data.containsKey("title")) {
      archive.setTitle((String) data.get("title"));
    }
    if (data.containsKey("salaryStandard")) {
      Object salaryObj = data.get("salaryStandard");
      if (salaryObj instanceof Number) {
        archive.setSalaryStandard(((Number) salaryObj).longValue());
      } else if (salaryObj instanceof String) {
        archive.setSalaryStandard(Long.parseLong((String) salaryObj));
      } else {
        archive.setSalaryStandard((Long) salaryObj);
      }
    }
    if (data.containsKey("nationality")) {
      archive.setNationality((String) data.get("nationality"));
    }
    if (data.containsKey("qualification")) {
      archive.setQualification((String) data.get("qualification"));
    }
    if (data.containsKey("email")) {
      archive.setEmail((String) data.get("email"));
    }
    if (data.containsKey("telephone")) {
      archive.setTelephone((String) data.get("telephone"));
    }
    if (data.containsKey("phone")) {
      archive.setPhone((String) data.get("phone"));
    }
    if (data.containsKey("address")) {
      archive.setAddress((String) data.get("address"));
    }
    if (data.containsKey("zipCode")) {
      archive.setZipCode((String) data.get("zipCode"));
    }
    if (data.containsKey("country")) {
      archive.setCountry((String) data.get("country"));
    }
    if (data.containsKey("birAddress")) {
      archive.setBirAddress((String) data.get("birAddress"));
    }
    if (data.containsKey("belief")) {
      archive.setBelief((String) data.get("belief"));
    }
    if (data.containsKey("identity")) {
      archive.setIdentity((String) data.get("identity"));
    }
    if (data.containsKey("major")) {
      archive.setMajor((String) data.get("major"));
    }
    if (data.containsKey("photoPath")) {
      archive.setPhotoPath((String) data.get("photoPath"));
    }
  }

  // 构建查询条件
  private Specification<ArchiveChange> buildQuerySpecification(ArchiveChangeQueryDTO queryDTO) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 按状态查询
      if (queryDTO.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
      }

      // 按申请人查询
      if (queryDTO.getApplyId() != null) {
        predicates.add(cb.equal(root.get("applyId"), queryDTO.getApplyId()));
      }

      // 按复核人查询
      if (queryDTO.getReviewId() != null) {
        predicates.add(cb.equal(root.get("reviewId"), queryDTO.getReviewId()));
      }

      // 按变更原因模糊查询
      if (StringUtils.hasText(queryDTO.getChangeReason())) {
        predicates.add(cb.like(root.get("changeReason"), "%" + queryDTO.getChangeReason() + "%"));
      }

      // 按创建时间范围查询
      if (queryDTO.getCreateTimeStart() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), queryDTO.getCreateTimeStart()));
      }
      if (queryDTO.getCreateTimeEnd() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), queryDTO.getCreateTimeEnd()));
      }

      // 如果有档案名称或档案编码条件，先查询档案ID
      if (StringUtils.hasText(queryDTO.getArchiveName()) || StringUtils.hasText(queryDTO.getArchiveCode())) {
        // 构建子查询：根据档案名称或编码查询档案ID
        javax.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
        javax.persistence.criteria.Root<Archive> archiveRoot = subquery.from(Archive.class);
        subquery.select(archiveRoot.get("arcId"));

        List<Predicate> archivePredicates = new ArrayList<>();

        if (StringUtils.hasText(queryDTO.getArchiveName())) {
          archivePredicates.add(cb.like(archiveRoot.get("name"), "%" + queryDTO.getArchiveName() + "%"));
        }
        if (StringUtils.hasText(queryDTO.getArchiveCode())) {
          archivePredicates.add(cb.like(archiveRoot.get("arcCode"), "%" + queryDTO.getArchiveCode() + "%"));
        }

        // 排除已删除的档案
        archivePredicates.add(cb.notEqual(archiveRoot.get("status"), 0));

        subquery.where(cb.and(archivePredicates.toArray(new Predicate[0])));

        // 添加主查询条件：archiveChange的arcId必须在子查询结果中
        predicates.add(cb.in(root.get("arcId")).value(subquery));
      } else {
        // 如果没有指定档案名称/编码，也排除已删除档案的变更申请
        javax.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
        javax.persistence.criteria.Root<Archive> archiveRoot = subquery.from(Archive.class);
        subquery.select(archiveRoot.get("arcId"));
        subquery.where(cb.notEqual(archiveRoot.get("status"), 0));

        predicates.add(cb.in(root.get("arcId")).value(subquery));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  // 补充关联信息
  private void enrichChangeWithRelatedInfo(ArchiveChange archiveChange) {
    try {
      // 获取档案信息
      Archive archive = archiveRepository.findById(archiveChange.getArcId()).orElse(null);
      if (archive != null) {
        archiveChange.setArchive(archive);
        archiveChange.setArchiveName(archive.getName());
        archiveChange.setArchiveCode(archive.getArcCode());

        // 补充档案的机构名称
        enrichArchiveWithOrgNames(archive);
      }

      // 获取申请人信息
      User applicant = userRepository.findById(archiveChange.getApplyId()).orElse(null);
      if (applicant != null) {
        archiveChange
            .setApplicantName(applicant.getUserName() != null ? applicant.getUserName() : applicant.getUserCode());
      }

      // 获取复核人信息
      if (archiveChange.getReviewId() != null) {
        User reviewer = userRepository.findById(archiveChange.getReviewId()).orElse(null);
        if (reviewer != null) {
          archiveChange
              .setReviewerName(reviewer.getUserName() != null ? reviewer.getUserName() : reviewer.getUserCode());
        }
      }

      // 设置状态名称
      String[] statusNames = { "", "待复核", "已通过", "已驳回" };
      if (archiveChange.getStatus() >= 1 && archiveChange.getStatus() < statusNames.length) {
        archiveChange.setStatusName(statusNames[archiveChange.getStatus()]);
      }

      // 强制解析JSON数据，确保前端能获取
      archiveChange.getBeforeDataParsed();
      archiveChange.getAfterDataParsed();
      archiveChange.getChangedFieldsParsed();

    } catch (Exception e) {
      log.error("补充变更申请关联信息失败", e);
    }
  }

  // 补充档案的机构名称
  private void enrichArchiveWithOrgNames(Archive archive) {
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
      log.error("补充档案机构名称失败", e);
    }
  }

}
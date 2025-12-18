// StandardService.java
package org.example.hrm.service;

import lombok.extern.slf4j.Slf4j;
import org.example.hrm.common.BusinessException;
import org.example.hrm.common.ResultCode;
import org.example.hrm.dto.OccupationDetailDTO;
import org.example.hrm.dto.StandardDTO;
import org.example.hrm.dto.StandardDetailResponseDTO;
import org.example.hrm.dto.StandardItemDTO;
import org.example.hrm.dto.StandardResponseDTO;
import org.example.hrm.entity.*;
import org.example.hrm.enums.SalaryEnums;
import org.example.hrm.repository.*;
import org.example.hrm.vo.PositionVO;
import org.example.hrm.vo.StandardItemVO;
import org.example.hrm.vo.StandardVO;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class StandardService {

    @PersistenceContext
    private EntityManager entityManager; // 注入 EntityManager

    @Autowired
    private StandardRepository standardRepository;

    @Autowired
    private StandardItemRepository standardItemRepository;

    @Autowired
    private StandardPositionRepository standardPositionRepository;

    @Autowired
    private StandardApprovalRepository standardApprovalRepository;

    @Autowired
    private StandardApprovalLogRepository standardApprovalLogRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private SalaryProjectRepository salaryProjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  OrganizationRepository orgRepository;

    @Autowired
    private UserService userService;

    /**
     * 生成标准编码
     */
    public String generateStandardCode() {
        String prefix = "STD";
        Date date = new Date();
        String dateStr = String.format("%tY%tm%td", date, date, date);
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + dateStr + random;
    }

   /**
 * 初始化薪酬标准表单所需的所有数据
 */
public Map<String, Object> initFormData() {
    log.info("初始化薪酬标准表单数据");
    System.out.println("\n=== 开始初始化表单数据 ===");
    
    Map<String, Object> result = new HashMap<>();
    
    try {
        // 1. 生成标准编码
        System.out.println("步骤1: 生成标准编码");
        String standardCode = this.generateStandardCode();
        System.out.println("生成的标准编码: " + standardCode);
        result.put("standardCode", standardCode);
        
        // 2. 获取所有职位数据
        System.out.println("\n步骤2: 获取职位数据");
        List<Map<String, Object>> positions = this.getAllPositionsWithOrg();
        System.out.println("职位数据查询完成，结果数量: " + positions.size());
        result.put("positions", positions);
        
        // 3. 获取所有薪酬项目数据
        System.out.println("\n步骤3: 获取项目数据");
        List<Map<String, Object>> projects = this.getAllSalaryProjects();
        System.out.println("项目数据查询完成，结果数量: " + projects.size());
        result.put("projects", projects);
        
        // 4. 获取所有枚举值
        System.out.println("\n步骤4: 获取枚举值");
        Map<String, Object> enums = this.getAllEnums();
        System.out.println("枚举数据获取完成，包含: " + enums.keySet());
        result.put("enums", enums);
        
        // 打印最终结果摘要
        System.out.println("\n=== 表单数据初始化结果摘要 ===");
        System.out.println("标准编码: " + standardCode);
        System.out.println("职位数量: " + positions.size());
        System.out.println("项目数量: " + projects.size());
        System.out.println("枚举类型: " + enums.keySet());
        System.out.println("初始化结果map中包含的key: " + result.keySet());
        
        log.info("表单数据初始化成功，职位数量：{}，项目数量：{}", 
                positions.size(), projects.size());
                
    } catch (Exception e) {
        System.out.println("\n=== 初始化表单数据失败 ===");
        System.out.println("异常类型: " + e.getClass().getName());
        System.out.println("异常信息: " + e.getMessage());
        System.out.println("堆栈跟踪:");
        e.printStackTrace();
        log.error("初始化表单数据失败：", e);
        throw new BusinessException(ResultCode.ERROR, "初始化表单数据失败：" + e.getMessage());
    }
    
    System.out.println("\n=== initFormData方法执行完成 ===");
    return result;
}

/**
 * 获取所有职位（包含机构层级信息和占用状态）
 */
public List<Map<String, Object>> getAllPositionsWithOrg() {
    log.info("获取所有职位（包含机构层级信息和占用状态）");
    System.out.println("\n=== 开始执行getAllPositionsWithOrg ===");

    try {
        if (positionRepository == null) {
            throw new BusinessException(ResultCode.ERROR, "positionRepository未注入");
        }
        
        // 获取所有启用的职位
        List<Position> positions = positionRepository.findAllActive();
        
        if (positions == null || positions.isEmpty()) {
            log.warn("数据库中未找到职位数据");
            return new ArrayList<>();
        }
        
        // 获取所有相关的机构ID（包括上级机构）
        Set<Long> orgIds = new HashSet<>();
        positions.forEach(position -> {
            orgIds.add(position.getOrgId());
        });
        
        // 批量查询机构信息
        List<Organization> organizations = orgRepository.findAllById(orgIds);
        Map<Long, Organization> orgMap = organizations.stream()
                .collect(Collectors.toMap(Organization::getOrgId, org -> org));
        
        // 获取所有已被占用的职位ID（使用新增的方法）
        List<Long> occupiedPositionIds = standardPositionRepository.findAllOccupiedPositionIds();
        Set<Long> occupiedSet = new HashSet<>(occupiedPositionIds);
        
        System.out.println("已占用的职位ID数量: " + occupiedSet.size());
        if (!occupiedSet.isEmpty()) {
            System.out.println("已占用的职位ID: " + occupiedSet);
        }
        
        // 转换为Map格式，包含机构层级信息和占用状态
        List<Map<String, Object>> result = new ArrayList<>();
        for (Position position : positions) {
            Map<String, Object> map = new HashMap<>();
            map.put("posId", position.getPosId());
            map.put("posCode", position.getPosCode());
            map.put("posName", position.getPosName());
            map.put("orgId", position.getOrgId());
            
            // 获取机构信息
            Organization org = orgMap.get(position.getOrgId());
            if (org != null) {
                map.put("orgName", org.getOrgName());
                map.put("orgCode", org.getOrgCode());
                map.put("orgLevel", org.getOrgLevel());
                map.put("parentId", org.getParId());
                map.put("orgFullPath", getOrgFullPath(org, orgMap));
                map.put("orgLevel", getOrgLevel(org, orgMap));
            } else {
                map.put("orgName", "");
                map.put("orgCode", "");
                map.put("orgType", 0);
                map.put("parentId", null);
                map.put("orgFullPath", "");
                map.put("orgLevel", 0);
            }
            
            // 设置占用状态
            boolean isOccupied = occupiedSet.contains(position.getPosId());
            map.put("isOccupied", isOccupied);
            map.put("occupied", isOccupied); // 兼容字段
            map.put("status", position.getStatus());
            
            // 如果是占用状态，获取占用详情
            if (isOccupied) {
                try {
                    List<OccupationDetailDTO> occupationDetails = 
                        standardPositionRepository.findOccupationDetailsByPositionId(position.getPosId(), null);
                    map.put("occupationDetails", occupationDetails);
                    map.put("occupiedCount", occupationDetails.size());
                } catch (Exception e) {
                    log.warn("获取职位占用详情失败，职位ID：{}", position.getPosId(), e);
                    map.put("occupationDetails", new ArrayList<>());
                    map.put("occupiedCount", 1);
                }
            } else {
                map.put("occupationDetails", new ArrayList<>());
                map.put("occupiedCount", 0);
            }
            
            result.add(map);
        }
        
        // 按机构层级和职位编码排序
        result.sort((a, b) -> {
            String orgPathA = (String) a.get("orgFullPath");
            String orgPathB = (String) b.get("orgFullPath");
            String posCodeA = (String) a.get("posCode");
            String posCodeB = (String) b.get("posCode");
            
            if (!orgPathA.equals(orgPathB)) {
                return orgPathA.compareTo(orgPathB);
            }
            return posCodeA.compareTo(posCodeB);
        });
        
        System.out.println("职位数据转换完成，返回结果数量: " + result.size() + 
                         ", 被占用数量: " + occupiedSet.size());
        log.info("成功获取职位数据，数量：{}，其中{}个已被占用", result.size(), occupiedSet.size());
        
        return result;
        
    } catch (Exception e) {
        log.error("获取职位数据失败：", e);
        throw new BusinessException(ResultCode.ERROR, "获取职位数据失败：" + e.getMessage());
    }
}

/**
 * 检查职位是否被占用
 */
public boolean checkPositionIsOccupied(Long positionId, Long excludeStandardId) {
    try {
        log.info("检查职位占用状态，positionId={}, excludeStandardId={}", positionId, excludeStandardId);
        
        if (excludeStandardId != null) {
            // 编辑模式：检查职位是否被其他标准占用（排除当前标准）
            return standardPositionRepository.existsByPositionIdAndStandardIdNot(positionId, excludeStandardId);
        } else {
            // 创建模式：检查职位是否被任何标准占用
            return standardPositionRepository.existsByPositionId(positionId);
        }
    } catch (Exception e) {
        log.error("检查职位占用状态失败，positionId={}", positionId, e);
        return false;
    }
}

/**
 * 批量检查职位占用状态
 */
public Map<Long, Boolean> batchCheckPositionsOccupied(List<Long> positionIds, Long excludeStandardId) {
    Map<Long, Boolean> result = new HashMap<>();
    
    try {
        log.info("批量检查职位占用状态，positionIds={}, excludeStandardId={}", positionIds, excludeStandardId);
        
        if (excludeStandardId != null) {
            // 编辑模式：排除当前标准
            for (Long positionId : positionIds) {
                boolean occupied = standardPositionRepository.existsByPositionIdAndStandardIdNot(positionId, excludeStandardId);
                result.put(positionId, occupied);
            }
        } else {
            // 创建模式：不排除任何标准
            // 先查询所有已占用的职位ID
            List<Long> occupiedIds = standardPositionRepository.findOccupiedPositionIdsByIds(positionIds);
            Set<Long> occupiedSet = new HashSet<>(occupiedIds);
            
            for (Long positionId : positionIds) {
                result.put(positionId, occupiedSet.contains(positionId));
            }
        }
        
        log.info("批量检查完成，结果：{}", result);
        return result;
        
    } catch (Exception e) {
        log.error("批量检查职位占用状态失败", e);
        // 出错时默认所有职位都可用
        for (Long positionId : positionIds) {
            result.put(positionId, false);
        }
        return result;
    }
}

/**
 * 获取职位占用详情
 */
public List<OccupationDetailDTO> getPositionOccupationDetails(Long positionId, Long excludeStandardId) {
    try {
        log.info("获取职位占用详情，positionId={}, excludeStandardId={}", positionId, excludeStandardId);
        return standardPositionRepository.findOccupationDetailsByPositionId(positionId, excludeStandardId);
    } catch (Exception e) {
        log.error("获取职位占用详情失败，positionId={}", positionId, e);
        return new ArrayList<>();
    }
}



/**
 * 获取机构的完整路径
 */
private String getOrgFullPath(Organization org, Map<Long, Organization> orgMap) {
    List<String> pathNames = new ArrayList<>();
    
    // 添加当前机构
    pathNames.add(org.getOrgName());
    
    // 递归添加上级机构
    Long parentId = org.getParId();
    while (parentId != null && parentId > 0) {
        Organization parentOrg = orgMap.get(parentId);
        if (parentOrg != null) {
            pathNames.add(0, parentOrg.getOrgName()); // 添加到前面
            parentId = parentOrg.getParId();
        } else {
            break;
        }
    }
    
    return String.join(" / ", pathNames);
}

/**
 * 获取机构层级
 */
private int getOrgLevel(Organization org, Map<Long, Organization> orgMap) {
    int level = 1; // 当前机构算一级
    
    // 递归计算上级机构
    Long parentId = org.getParId();
    while (parentId != null && parentId > 0) {
        Organization parentOrg = orgMap.get(parentId);
        if (parentOrg != null) {
            level++;
            parentId = parentOrg.getParId();
        } else {
            break;
        }
    }
    
    return level;
}

/**
 * 获取所有薪酬项目（用于选择）
 */
public List<Map<String, Object>> getAllSalaryProjects() {
    log.info("获取所有薪酬项目");
    System.out.println("\n=== 开始执行getAllSalaryProjects ===");
    
    try {
        // 检查Repository是否注入成功
        if (salaryProjectRepository == null) {
            System.out.println("ERROR: salaryProjectRepository 为空，未成功注入！");
            throw new BusinessException(ResultCode.ERROR, "salaryProjectRepository未注入");
        }
        
        System.out.println("salaryProjectRepository类名: " + salaryProjectRepository.getClass().getName());
        
        // 测试不同的查询方法
        System.out.println("\n--- 测试查询方法 ---");
        
        // 方法1：使用findByStatus(1)
        System.out.println("尝试调用 salaryProjectRepository.findByStatus(1)...");
        try {
            List<SalaryProject> projects = salaryProjectRepository.findByStatus(1);
            System.out.println("findByStatus(1)查询结果: " + projects);
            System.out.println("返回类型: " + (projects != null ? projects.getClass().getName() : "null"));
            System.out.println("数据数量: " + (projects != null ? projects.size() : 0));
            
            if (projects == null) {
                System.out.println("WARNING: findByStatus返回null");
                projects = new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.out.println("findByStatus调用失败: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        // 方法2：使用findAll
        System.out.println("\n尝试调用 salaryProjectRepository.findAll()...");
        try {
            List<SalaryProject> allProjects = salaryProjectRepository.findAll();
            System.out.println("findAll查询结果数量: " + (allProjects != null ? allProjects.size() : 0));
            if (allProjects != null && !allProjects.isEmpty()) {
                System.out.println("第一条记录: ID=" + allProjects.get(0).getProjectId() + 
                                 ", Name=" + allProjects.get(0).getProjectName());
            }
        } catch (Exception e) {
            System.out.println("findAll调用失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
        
        // 正式查询
        System.out.println("\n--- 正式获取项目数据 ---");
        List<SalaryProject> projects = salaryProjectRepository.findByStatus(1);
        
        System.out.println("查询返回结果: " + projects);
        
        if (projects == null) {
            System.out.println("查询返回null，创建空列表");
            projects = new ArrayList<>();
        }
        
        System.out.println("查询到的项目数量: " + projects.size());
        
        if (projects.isEmpty()) {
            System.out.println("项目列表为空");
            log.warn("数据库中未找到薪酬项目数据");
            return new ArrayList<>();
        }
         // 筛选category为"基本工资"和"奖金"的项目
        System.out.println("\n--- 筛选category为'基本工资'和'奖金'的项目 ---");
        List<SalaryProject> filteredProjects = new ArrayList<>();
        List<SalaryProject> excludedProjects = new ArrayList<>();
        
        for (SalaryProject project : projects) {
            String category = project.getCategory();
            if ("基本工资".equals(category) || "奖金".equals(category) || "津贴".equals(category) ||"津社保贴".equals(category)) {
                filteredProjects.add(project);
            } else {
                excludedProjects.add(project);
            }
        }

         List<SalaryProject> Fprojects = filteredProjects;
        
        if (Fprojects.isEmpty()) {
            System.out.println("筛选后的项目列表为空");
            log.warn("数据库中未找到薪酬项目数据（category为'基本工资'或'奖金'）");
            return new ArrayList<>();
        }
        
        // 打印详细项目信息
        System.out.println("\n详细项目信息:");
        for (int i = 0; i < Math.min(Fprojects.size(), 5); i++) {
            SalaryProject project = Fprojects.get(i);
            System.out.println(String.format("  [%d] ID=%d, 编码=%s, 名称=%s, 类型=%d, 状态=%d", 
                i+1, project.getProjectId(), project.getProjectCode(), 
                project.getProjectName(), project.getProjectType(), project.getStatus()));
        }
        if (Fprojects.size() > 5) {
            System.out.println("  ... 还有" + (Fprojects.size() - 5) + "条记录");
        }
        
        // 转换为Map格式
        System.out.println("\n开始转换项目数据到Map格式...");
        List<Map<String, Object>> result = new ArrayList<>();
        for (SalaryProject project : Fprojects) {
            Map<String, Object> map = new HashMap<>();
            map.put("projectId", project.getProjectId());
            map.put("projectCode", project.getProjectCode());
            map.put("projectName", project.getProjectName());
            map.put("projectType", project.getProjectType());
            map.put("projectTypeName", this.getProjectTypeName(project.getProjectType()));
            map.put("category", project.getCategory());
            map.put("calculationMethod", project.getCalculationMethod());
            map.put("description", project.getDescription());
            map.put("isRequired", project.getStatus());
            map.put("sortOrder", project.getSortOrder());
            result.add(map);
            
            // 打印转换后的第一条记录
            if (result.size() == 1) {
                System.out.println("第一条转换后的数据: " + map);
            }
        }
        
        // 按sortOrder排序
        System.out.println("按sortOrder排序...");
        result.sort(Comparator.comparingInt(m -> (Integer) m.get("sortOrder")));
        
        System.out.println("项目数据转换完成，返回结果数量: " + result.size());
        log.info("成功获取薪酬项目数据，数量：{}", result.size());
        
        System.out.println("=== getAllSalaryProjects执行完成 ===");
        return result;
        
    } catch (Exception e) {
        System.out.println("\n=== getAllSalaryProjects执行失败 ===");
        System.out.println("异常类型: " + e.getClass().getName());
        System.out.println("异常信息: " + e.getMessage());
        System.out.println("堆栈跟踪:");
        e.printStackTrace();
        log.error("获取薪酬项目数据失败：", e);
        throw new BusinessException(ResultCode.ERROR, "获取薪酬项目数据失败：" + e.getMessage());
    }
}




    /**
     * 获取项目类型名称
     */
    private String getProjectTypeName(Integer projectType) {
        if (projectType == null) return "";
        
        switch (projectType) {
            case 1: return "增项";
            case 2: return "减项";
            default: return "其他";
        }
    }

    /**
     * 获取所有枚举值
     */
    public Map<String, Object> getAllEnums() {
        Map<String, Object> enums = new HashMap<>();
        
        // 标准状态
        enums.put("standardStatuses", SalaryEnums.StandardStatus.toList());
        
        // 项目类型
        enums.put("projectTypes", SalaryEnums.ProjectType.toList());
        
        // 项目类别
        enums.put("projectCategories", SalaryEnums.ProjectCategory.toList());
        
        // 计算方式
        enums.put("calculationMethods", SalaryEnums.CalculationMethod.toList());
        
        return enums;
    }

    /**
     * 获取所有职位（用于前端选择）
     */
    public List<Map<String, Object>> getPositionsForSelection() {
        return this.getAllPositionsWithOrg();
    }

    /**
     * 获取薪酬项目用于选择
     */
    public List<Map<String, Object>> getProjectsForSelection() {
        return this.getAllSalaryProjects();
    }

    

    /**
     * 创建薪酬标准
     */
    @Transactional
    public StandardVO createStandard(StandardDTO dto, Long creatorId) {
        log.info("创建薪酬标准，创建人ID：{}", creatorId);
        
        try {
            // 1. 验证职位ID列表
            if (dto.getPositionIds() == null || dto.getPositionIds().isEmpty()) {
                throw new BusinessException(ResultCode.POSITION_LESS, "请至少选择一个职位");
            }

            // 2. 验证明细项目
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "请至少配置一个薪酬项目");
            }

            // 3. 验证职位是否存在
            List<Long> positionIds = dto.getPositionIds();
            for (Long positionId : positionIds) {
                positionRepository.findById(positionId)
                        .orElseThrow(() -> new BusinessException(ResultCode.POSITION_NOT_EXIST, 
                                "职位不存在，ID：" + positionId));
            }

            // 4. 创建标准主记录
            Standard standard = new Standard();
            standard.setStandardCode(dto.getStandardCode());
            standard.setStandardName(dto.getStandardName());
            standard.setCreatorId(creatorId);
            standard.setStatus(1); // 草稿状态
            standard.setCreatedAt(LocalDateTime.now());
            standard.setUpdatedAt(LocalDateTime.now());

            Standard savedStandard = standardRepository.save(standard);
            log.info("薪酬标准主记录创建成功，ID：{}，编码：{}", 
                    savedStandard.getStandardId(), savedStandard.getStandardCode());

            // 5. 保存关联职位
            saveStandardPositions(savedStandard, positionIds);

            // 6. 保存明细项目
            saveStandardItems(savedStandard, dto.getItems());

            // 7. 转换并返回VO
            return convertToVO(savedStandard);

        } catch (BusinessException e) {
            log.error("创建薪酬标准业务异常：{}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("创建薪酬标准失败：", e);
            throw new BusinessException(ResultCode.ERROR, "创建薪酬标准失败：" + e.getMessage());
        }
    }

    /**
     * 保存标准关联职位
     */
    private void saveStandardPositions(Standard standard, List<Long> positionIds) {
        List<StandardPosition> positions = new ArrayList<>();
        for (Long positionId : positionIds) {
           // 先查询职位实体
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(ResultCode.POSITION_NOT_EXIST, 
                        "职位不存在，ID：" + positionId));
        
        StandardPosition sp = StandardPosition.builder()
                .standard(standard)
                .position(position)  // 传入 Position 实体，不是 positionId
                .build();
        positions.add(sp);
        }
        standardPositionRepository.saveAll(positions);
        log.info("保存关联职位成功，数量：{}", positions.size());
    }

    /**
     * 保存标准明细项目
     */
    private void saveStandardItems(Standard standard, List<StandardItemDTO> itemDTOs) {
        List<StandardItem> items = new ArrayList<>();
        
        for (StandardItemDTO dto : itemDTOs) {
            // 验证项目是否存在
            SalaryProject project = salaryProjectRepository.findByProjectCode(dto.getProjectCode())
                    .orElseThrow(() -> new BusinessException(ResultCode.SALARY_ITEM_NOT_FOUND,
                            "薪酬项目不存在：" + dto.getProjectCode()));

            // 创建明细项
            StandardItem item = StandardItem.builder()
                    .standard(standard)
                    .project(project)
                    .projectCode(dto.getProjectCode())
                    .amount(dto.getAmount())
                    .unitPrice(dto.getUnitPrice())
                    .ratio(dto.getRatio())
                    .calculationMethod(dto.getCalculationMethod())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .build();
            
            items.add(item);
        }
        
        standardItemRepository.saveAll(items);
        log.info("保存明细项目成功，数量：{}", items.size());
    }

    /**
     * 更新薪酬标准
     */
    @Transactional
    public StandardVO updateStandard(Long standardId, StandardDTO dto, Long updaterId) {
        log.info("更新薪酬标准，标准ID：{}，更新人：{}", standardId, updaterId);
        
        try {
            // 1. 获取标准记录
            Standard standard = standardRepository.findById(standardId)
                    .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                            "薪酬标准不存在"));

                            System.out.println("原始数据名称：" + standard.getStandardName() +"，状态:"+ standard.getStatus());
                            System.out.println("新数据名称：" + dto.getStandardName() );
            // 2. 检查状态（只有草稿和驳回状态可以编辑）
        if (standard.getStatus() != 1 && standard.getStatus() != 0) {
            throw new BusinessException(ResultCode.SALARY_STANDARD_APPROVED, 
                    "当前状态不能编辑，只有草稿或驳回状态可修改");
        }

        // 3. 验证职位数据（新增）
        if (dto.getPositionIds() == null || dto.getPositionIds().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请至少选择一个适用职位");
        }

        // 4. 验证职位是否被其他标准占用（关键逻辑）
        for (Long positionId : dto.getPositionIds()) {
            // 检查职位是否存在
            Position position = positionRepository.findById(positionId)
                    .orElseThrow(() -> new BusinessException(ResultCode.POSITION_NOT_EXIST, 
                            "职位不存在，ID：" + positionId));
            
            // 检查是否被其他标准占用（排除当前标准）
            boolean isOccupied = standardPositionRepository.existsByPositionIdAndStandardIdNot(positionId, standardId);
            if (isOccupied) {
                throw new BusinessException(ResultCode.DUPLICATE_KEY, 
                        "职位【" + position.getPosName() + "】已被其他薪酬标准占用");
            }
        }

         // 5. 关键：强制删除旧关联（绕过Hibernate缓存）
            log.info("强制删除旧关联记录...");
            standardPositionRepository.deleteByStandardIdNative(standardId);
            standardItemRepository.deleteByStandardIdNative(standardId);

             // 6. 关键：更新基本信息后立即刷新（确保Hibernate检测到变化）
        standard.setStandardName(dto.getStandardName().trim());
        standard.setCreatorId(updaterId);
        standard.setUpdatedAt(LocalDateTime.now());

        System.out.println("更新后数据名称："+ standard.getStandardName());
            
            // 6. 立即刷新Session，确保删除执行
            standard = standardRepository.saveAndFlush(standard);
            entityManager.flush(); // 强制立即执行SQL
            entityManager.clear(); // 清除缓存

            // 3. 更新基本信息
            standard.setStandardName(dto.getStandardName().trim());
            standard.setCreatorId(updaterId);
            standard.setUpdatedAt(LocalDateTime.now());

              // 8. 重新获取对象（避免缓存干扰）
        standard = standardRepository.findById(standardId)
                .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, "薪酬标准不存在"));

         // 8. 重新保存职位关联（去重）
            Set<Long> uniquePositionIds = new LinkedHashSet<>(dto.getPositionIds());
            List<StandardPosition> positions = new ArrayList<>();
            for (Long positionId : uniquePositionIds) {
                Position position = positionRepository.findById(positionId)
                        .orElseThrow(() -> new BusinessException(ResultCode.POSITION_NOT_EXIST, "职位不存在，ID：" + positionId));
                
                StandardPosition sp = new StandardPosition();
                sp.setStandard(standard);
                sp.setPosition(position);
                positions.add(sp);
            }
            
            if (!positions.isEmpty()) {
                standardPositionRepository.saveAll(positions);
            }
            
            // 9. 重新保存明细项目
            List<StandardItem> items = new ArrayList<>();
            for (StandardItemDTO itemDTO : dto.getItems()) {
                SalaryProject project = salaryProjectRepository.findByProjectCode(itemDTO.getProjectCode())
                        .orElseThrow(() -> new BusinessException(ResultCode.SALARY_ITEM_NOT_FOUND, "项目不存在：" + itemDTO.getProjectCode()));
                
                StandardItem item = StandardItem.builder()
                        .standard(standard)
                        .project(project)
                        .projectCode(itemDTO.getProjectCode())
                        .amount(itemDTO.getAmount())
                        .unitPrice(itemDTO.getUnitPrice())
                        .ratio(itemDTO.getRatio())
                        .calculationMethod(itemDTO.getCalculationMethod())
                        .sortOrder(itemDTO.getSortOrder() != null ? itemDTO.getSortOrder() : 0)
                        .build();
                items.add(item);
            }
            
            if (!items.isEmpty()) {
                standardItemRepository.saveAll(items);
            }
            
            
            // 11. 最终保存
            standard = standardRepository.saveAndFlush(standard);
        entityManager.refresh(standard); 
        entityManager.flush(); // 强制立即执行SQL
            
            log.info("=== 更新成功，标准ID: {}, 名称: {} ===", standard.getStandardId(), standard.getStandardName());
            
            return convertToVO(standard);

        } catch (BusinessException e) {
            log.error("更新薪酬标准业务异常：{}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("更新薪酬标准失败：", e);
            throw new BusinessException(ResultCode.ERROR, "更新薪酬标准失败：" + e.getMessage());
        }
    }

    /**
 * 提交审核
 */
@Transactional
public void submitForApproval(Long standardId, Long submitterId, String comment) {
    log.info("提交审核，标准ID：{}，提交人：{}", standardId, submitterId);
    
    try {
        // 1. 获取标准记录
        Standard standard = standardRepository.findById(standardId)
                .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                        "薪酬标准不存在"));

        // 2. 检查状态（只有草稿和驳回状态可以提交）
        if (standard.getStatus() != 1 && standard.getStatus() != 0) {
            throw new BusinessException(ResultCode.SALARY_STANDARD_APPROVED, 
                    "当前状态不能提交审核");
        }

        // 3. 获取提交人信息（当前操作用户）
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST,"提交人不存在"));

                // 4. 查找薪酬经理（roleType=5）作为审核人 - 修改这里
        User approver = userRepository.findByUserCode("salary_manager")
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST, 
                        "未找到薪酬经理，请联系管理员"));

        // 4. 更新标准状态为待审核
        standard.setStatus(2); // 待审核状态
        standard.setUpdatedAt(LocalDateTime.now());
        standardRepository.save(standard);

        // 5. 创建审核记录 - 使用当前操作用户作为提交人
        StandardApproval approval = StandardApproval.builder()
                .standard(standard)
                .submitterId(submitterId) // 当前操作用户ID
                .submitterName(submitter.getUserName()) // 当前操作用户姓名
                .submitTime(LocalDateTime.now())
                 .approverId(approver.getUserId())  // 自动分配的审核人ID
                .approverName(approver.getUserName()) // 自动分配的审核人姓名
                .approvalStatus(2) // 待审核
                .approvalOpinion(comment != null ? comment : "提交审核，等待审批")
                .isActive(1)
                .build();
        standardApprovalRepository.save(approval);

        // 6. 记录操作日志
        StandardApprovalLog approvalLog = StandardApprovalLog.builder()
                .approvalId(approval.getId())
                .operatorId(submitterId) // 当前操作用户ID
                .operatorName(submitter.getUserName()) // 当前操作用户姓名
                .operationType("Submit")
                .operationContent("提交审核" + (StringUtils.hasText(comment) ? "，备注：" + comment : ""))
                .operationTime(LocalDateTime.now())
                .build();
        standardApprovalLogRepository.save(approvalLog);

        log.info("提交审核成功，标准ID：{}，提交人：{}", standardId, submitter.getUserName());

    } catch (BusinessException e) {
        log.error("提交审核业务异常：{}", e.getMessage(), e);
        throw e;
    } catch (Exception e) {
        log.error("提交审核失败：", e);
        throw new BusinessException(ResultCode.ERROR, "提交审核失败：" + e.getMessage());
    }
}
    /**
     * 审核通过
     */
    @Transactional
    public void approveStandard(Long standardId, Long approverId, String opinion) {
        log.info("审核通过，标准ID：{}，审核人：{}", standardId, approverId);
        
        try {

        // 验证审核意见
                 if (opinion == null || opinion.trim().isEmpty()) {
                    throw new BusinessException(ResultCode.ERROR,"审核意见不能为空");
                }

                // 获取审核人信息
            User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_EXIST,"审核人不存在"));
            // 1. 获取标准记录
            Standard standard = standardRepository.findById(standardId)
                    .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                            "薪酬标准不存在"));

            // 2. 检查状态（只有待审核状态可以审核）
            if (standard.getStatus() != 2) {
                throw new BusinessException(ResultCode.SALARY_STANDARD_APPROVED, 
                        "当前状态不能进行审核操作");
            }

            // 3. 更新标准状态
            standard.setStatus(3); // 已生效状态
           // standard.setRegistrarId(approverId);
            standard.setRegistrationTime(LocalDateTime.now());
           // standard.setUpdatedAt(LocalDateTime.now());
            standardRepository.save(standard);

            // 4. 更新审核记录
            StandardApproval approval = standardApprovalRepository.findActiveApproval(standardId)
                    .orElseThrow(() -> new BusinessException(ResultCode.ERROR, "未找到审核记录"));
            
            approval.setApproverId(approverId);
            approval.setApprovalTime(LocalDateTime.now());
            approval.setApprovalStatus(3); // 审核通过
            approval.setApprovalOpinion(opinion);
            approval.setIsActive(1);
            standardApprovalRepository.save(approval);

            // 5. 记录操作日志
            StandardApprovalLog approvalLog = StandardApprovalLog.builder()
                    .approvalId(approval.getId())
                    .operatorId(approverId)
                    .operationType("Approve")
                    .operationContent("审核通过" + (StringUtils.hasText(opinion) ? "，意见：" + opinion : ""))
                    .build();
            standardApprovalLogRepository.save(approvalLog);

            log.info("审核通过成功，标准ID：{}，审核人：{}", standardId,approver.getUserName());

        } catch (BusinessException e) {
            log.error("审核通过业务异常：{}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("审核通过失败：", e);
            throw new BusinessException(ResultCode.ERROR, "审核通过失败：" + e.getMessage());
        }
    }

    /**
     * 审核驳回
     */
    @Transactional
    public void rejectStandard(Long standardId, Long approverId, String opinion) {
        log.info("审核驳回，标准ID：{}，审核人：{}", standardId, approverId);
        
        try {
            // 1. 获取标准记录
            Standard standard = standardRepository.findById(standardId)
                    .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                            "薪酬标准不存在"));

            // 2. 检查状态（只有待审核状态可以审核）
            if (standard.getStatus() != 2) {
                throw new BusinessException(ResultCode.SALARY_STANDARD_APPROVED, 
                        "当前状态不能进行审核操作");
            }

            // 3. 更新标准状态
            standard.setStatus(0); // 驳回状态
            standard.setUpdatedAt(LocalDateTime.now());
            standardRepository.save(standard);

            // 4. 更新审核记录
            StandardApproval approval = standardApprovalRepository.findActiveApproval(standardId)
                    .orElseThrow(() -> new BusinessException(ResultCode.ERROR, "未找到审核记录"));
            
            approval.setApproverId(approverId);
            approval.setApprovalTime(LocalDateTime.now());
            approval.setApprovalStatus(0); // 驳回
            approval.setApprovalOpinion(opinion);
            approval.setIsActive(1);
            standardApprovalRepository.save(approval);

            // 5. 记录操作日志
            StandardApprovalLog approvalLog = StandardApprovalLog.builder()
                    .approvalId(approval.getId())
                    .operatorId(approverId)
                    .operationType("Reject")
                    .operationContent("审核驳回" + (StringUtils.hasText(opinion) ? "，意见：" + opinion : ""))
                    .build();
            standardApprovalLogRepository.save(approvalLog);

            log.info("审核驳回成功，标准ID：{}", standardId);

        } catch (BusinessException e) {
            log.error("审核驳回业务异常：{}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("审核驳回失败：", e);
            throw new BusinessException(ResultCode.ERROR, "审核驳回失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取标准详情
     */
    public StandardVO getStandardById(Long standardId) {
        log.info("获取标准详情，ID：{}", standardId);
        
        Standard standard = standardRepository.findById(standardId)
                .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                        "薪酬标准不存在"));
        
        return convertToVO(standard);
    }

    /**
     * 根据编码获取标准详情
     */
    public StandardVO getStandardByCode(String standardCode) {
        log.info("获取标准详情，编码：{}", standardCode);
        
        Standard standard = standardRepository.findByStandardCode(standardCode)
                .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                        "薪酬标准不存在"));
        
        return convertToVO(standard);
    }
    

    /**
     * 获取所有启用的薪酬标准
     */
    public List<StandardVO> getActiveStandards() {
        log.info("获取所有启用的薪酬标准");
        
        List<Standard> standards = standardRepository.findByStatus(3); // 已生效状态
        
        return standards.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取职位关联的启用的薪酬标准
     */
    public List<StandardVO> getActiveStandardsByPositionId(Integer positionId) {
        log.info("获取职位关联的启用的薪酬标准，职位ID：{}", positionId);
        
        List<Standard> standards = standardRepository.findActiveByPositionId(positionId);
        
        return standards.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
 * 将 Standard 转换为 StandardResponseDTO
 */
// 修改 convertToResponseDTO 方法，确保 positions 和 items 正确获取
public StandardResponseDTO convertToResponseDTO(Standard standard) {
    if (standard == null) {
        return null;
    }
    
    StandardResponseDTO dto = new StandardResponseDTO();
    dto.setStandardId(standard.getStandardId());
    dto.setStandardCode(standard.getStandardCode());
    dto.setStandardName(standard.getStandardName());
    dto.setCreatorId(standard.getCreatorId());
    dto.setRegistrarId(standard.getRegistrarId());
    dto.setStatus(standard.getStatus());
    dto.setRegistrationTime(standard.getRegistrationTime());
    dto.setCreatedAt(standard.getCreatedAt());
    dto.setUpdatedAt(standard.getUpdatedAt());
    
    System.out.println("转换标准: " + standard.getStandardCode());
    
    // 设置创建人名称
    if (standard.getCreatorId() != null) {
        try {
            String creatorName = userService.getUserNameById(standard.getCreatorId());
            dto.setCreatorName(creatorName);
            System.out.println("创建人: " + creatorName);
        } catch (Exception e) {
            log.warn("获取创建人名称失败，ID：{}", standard.getCreatorId());
            dto.setCreatorName("未知");
        }
    }
    
    // 设置登记人名称
    if (standard.getRegistrarId() != null) {
        try {
            String registrarName = userService.getUserNameById(standard.getRegistrarId());
            dto.setRegistrarName(registrarName);
        } catch (Exception e) {
            log.warn("获取登记人名称失败，ID：{}", standard.getRegistrarId());
            dto.setRegistrarName("未知");
        }
    }
    
    // 查询关联职位
    try {
        List<StandardPosition> positions = standardPositionRepository.findByStandard(standard);
        System.out.println("查询到职位数量: " + positions.size());
        
        List<StandardResponseDTO.PositionInfoDTO> positionDTOs = new ArrayList<>();
        for (StandardPosition sp : positions) {
            StandardResponseDTO.PositionInfoDTO positionDTO = new StandardResponseDTO.PositionInfoDTO();
            if (sp.getPosition() != null) {
                positionDTO.setPosId(sp.getPosition().getPosId());
                positionDTO.setPosCode(sp.getPosition().getPosCode());
                positionDTO.setPosName(sp.getPosition().getPosName());
                positionDTO.setOrgId(sp.getPosition().getOrgId());
                positionDTO.setOrgName(sp.getPosition().getOrgName() != null ? 
                    sp.getPosition().getOrgName() : "");
                positionDTOs.add(positionDTO);
            }
        }
        dto.setPositions(positionDTOs);
    } catch (Exception e) {
        System.out.println("查询职位失败: " + e.getMessage());
        dto.setPositions(new ArrayList<>());
    }
    
    // 查询项目统计信息
    try {
        List<StandardItem> items = standardItemRepository.findByStandard(standard);
        dto.setProjectCount(items.size());
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StandardItem item : items) {
            if (item.getAmount() != null) {
                totalAmount = totalAmount.add(item.getAmount());
            }
        }
        dto.setTotalAmount(totalAmount);
    } catch (Exception e) {
        System.out.println("查询项目失败: " + e.getMessage());
        dto.setProjectCount(0);
        dto.setTotalAmount(BigDecimal.ZERO);
    }
    
    return dto;
}

/**
 * 将 Standard 列表转换为 StandardResponseDTO 列表
 */
public List<StandardResponseDTO> convertToResponseDTOList(List<Standard> standards) {
    if (standards == null || standards.isEmpty()) {
        return new ArrayList<>();
    }
    
    return standards.stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
}

/**
 * 获取标准详情（包含所有关联数据）
 */
@Transactional(readOnly = true)
public StandardDetailResponseDTO getStandardDetailById(Long standardId) {
    log.info("获取标准详情（含关联数据），ID：{}", standardId);
    
    try {
        // 使用 JPQL 查询一次性加载所有关联数据
        Standard standard = standardRepository.findByIdWithAssociations(standardId)
                .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                        "薪酬标准不存在"));
        
        return convertToDetailResponseDTO(standard);
    } catch (Exception e) {
        log.error("获取标准详情失败，ID：{}", standardId, e);
        throw new BusinessException(ResultCode.ERROR, "获取标准详情失败：" + e.getMessage());
    }
}

/**
 * 获取标准审核详情（专为审核页面设计）
 */
/**
 * 获取标准审核详情（简单版本）
 */
@Transactional(readOnly = true)
public StandardDetailResponseDTO getStandardApprovalDetail(Long standardId) {
    log.info("获取标准审核详情，ID：{}", standardId);
    
    try {
        // 1. 获取标准基本信息
        Standard standard = standardRepository.findById(standardId)
                .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                        "薪酬标准不存在"));
        
        // 2. 获取标准项目明细
        List<StandardItem> items = standardItemRepository.findByStandardWithProject(standard);
        standard.setItems(items);
        
        // 3. 获取关联职位
        List<StandardPosition> positions = standardPositionRepository.findByStandard(standard);
        standard.setStandardPositions(positions);
        
        // 4. 获取审核记录
        List<StandardApproval> approvals = standardApprovalRepository.findByStandardId(standardId);
        standard.setApprovals(approvals);
        
        // 5. 转换为响应DTO
        StandardDetailResponseDTO dto = convertToDetailResponseDTO(standard);
        
        // 6. 获取当前活跃的审核记录
        List<StandardApproval> activeApprovals = standardApprovalRepository.findActiveApprovalsByStandardId(standardId);
        if (!activeApprovals.isEmpty()) {
            dto.setCurrentApproval(convertApprovalToDTO(activeApprovals.get(0)));
        }
        
        // 7. 获取所有审核日志
        List<StandardApprovalLog> logs = standardApprovalLogRepository.findByStandardId(standardId);
        List<StandardDetailResponseDTO.ApprovalLogDTO> logDTOs = logs.stream()
                .map(this::convertApprovalLogToDTO)
                .collect(Collectors.toList());
        dto.setApprovalLogs(logDTOs);
        
        return dto;
    } catch (Exception e) {
        log.error("获取标准审核详情失败，ID：{}", standardId, e);
        throw new BusinessException(ResultCode.ERROR, "获取标准审核详情失败：" + e.getMessage());
    }
}
/**
 * 获取标准审核历史记录
 */
@Transactional(readOnly = true)
public List<StandardDetailResponseDTO.ApprovalHistoryDTO> getApprovalHistory(Long standardId) {
    log.info("获取标准审核历史记录，ID：{}", standardId);
    
    try {
        // 获取所有审核记录（按提交时间倒序）
        List<StandardApproval> approvals = standardApprovalRepository
                .findByStandardIdOrderBySubmitTimeDesc(standardId);

                  System.out.println("查询到 "+approvals.size()+" 条审核记录");
        return approvals.stream()
                .map(this::convertApprovalToHistoryDTO)
                .collect(Collectors.toList());
    } catch (Exception e) {
        log.error("获取审核历史记录失败，ID：{}", standardId, e);
        throw new BusinessException(ResultCode.ERROR, "获取审核历史记录失败：" + e.getMessage());
    }
}

/**
 * 获取详细的审核日志（包含操作详情）
 */
@Transactional(readOnly = true)
public List<StandardDetailResponseDTO.ApprovalLogDTO> getDetailedApprovalLogs(Long standardId) {
    log.info("获取详细审核日志，标准ID：{}", standardId);
    
    try {
        List<StandardApprovalLog> logs = standardApprovalLogRepository.findByStandardId(standardId);
        return logs.stream()
                .map(this::convertApprovalLogToDTO)
                .collect(Collectors.toList());
    } catch (Exception e) {
        log.error("获取审核日志失败，标准ID：{}", standardId, e);
        throw new BusinessException(ResultCode.ERROR, "获取审核日志失败：" + e.getMessage());
    }
}

/**
 * 转换审核日志为DTO
 */
private StandardDetailResponseDTO.ApprovalLogDTO convertApprovalLogToDTO(StandardApprovalLog logsLog) {
    StandardDetailResponseDTO.ApprovalLogDTO dto = 
        StandardDetailResponseDTO.ApprovalLogDTO.builder()
            .id(logsLog.getLogId())
            .approvalId(logsLog.getApprovalId())
            .operatorId(logsLog.getOperatorId())
            .operationType(logsLog.getOperationType())
            .operationContent(logsLog.getOperationContent())
            .operationTime(logsLog.getOperationTime())
            .build();
    
    // 设置操作人名称 - 优先使用日志中存储的姓名
    if (logsLog.getOperatorName() != null && !logsLog.getOperatorName().isEmpty()) {
        dto.setOperatorName(logsLog.getOperatorName());
    } else if (logsLog.getOperatorId() != null) {
        // 如果日志中没有存储姓名，则从用户服务获取
        try {
            String operatorName = userService.getUserNameById(logsLog.getOperatorId());
            dto.setOperatorName(operatorName);
        } catch (Exception e) {
            log.warn("获取操作人名称失败，ID：{}", logsLog.getOperatorId());
            dto.setOperatorName("未知");
        }
    }
    
    return dto;
}

/**
 * 转换为审核历史DTO
 */
private StandardDetailResponseDTO.ApprovalHistoryDTO convertApprovalToHistoryDTO(StandardApproval approval) {
    StandardDetailResponseDTO.ApprovalHistoryDTO dto = 
        new StandardDetailResponseDTO.ApprovalHistoryDTO();
    dto.setId(approval.getId());
    dto.setStandardId(approval.getStandard().getStandardId());
    dto.setSubmitterId(approval.getSubmitterId());
    dto.setApproverId(approval.getApproverId());
    dto.setSubmitTime(approval.getSubmitTime());
    dto.setApprovalTime(approval.getApprovalTime());
    dto.setApprovalStatus(approval.getApprovalStatus());
    dto.setApprovalOpinion(approval.getApprovalOpinion());
    dto.setIsActive(approval.getIsActive());
    
    // 设置提交人名称
    dto.setSubmitterName(approval.getSubmitterName());
    
    // 设置审核人名称
    if (approval.getApproverId() != null) {
        try {
            String approverName = userService.getUserNameById(approval.getApproverId());
            dto.setApproverName(approverName);
        } catch (Exception e) {
            log.warn("获取审核人名称失败，ID：{}", approval.getApproverId());
            dto.setApproverName("未知");
        }
    } else {
        dto.setApproverName("待审核");
    }
    
    // 获取该审核记录的所有日志
    List<StandardApprovalLog> logs = standardApprovalLogRepository.findByStandardId(approval.getId());
    List<StandardDetailResponseDTO.ApprovalLogDTO> logDTOs = logs.stream()
            .map(this::convertApprovalLogToDTO)
            .collect(Collectors.toList());
    dto.setLogs(logDTOs);
    
    return dto;
}

/**
 * 转换为详情DTO（安全的方式，不直接暴露实体）
 */
private StandardDetailResponseDTO convertToDetailResponseDTO(Standard standard) {
    if (standard == null) {
        return null;
    }
    
    StandardDetailResponseDTO dto = new StandardDetailResponseDTO();
    dto.setStandardId(standard.getStandardId());
    dto.setStandardCode(standard.getStandardCode());
    dto.setStandardName(standard.getStandardName());
    dto.setCreatorId(standard.getCreatorId());
    dto.setRegistrarId(standard.getRegistrarId());
    dto.setStatus(standard.getStatus());
    dto.setRegistrationTime(standard.getRegistrationTime());
    dto.setCreatedAt(standard.getCreatedAt());
    dto.setUpdatedAt(standard.getUpdatedAt());
    
    // 设置创建人名称
    if (standard.getCreatorId() != null) {
        try {
            String creatorName = userService.getUserNameById(standard.getCreatorId());
            dto.setCreatorName(creatorName);
        } catch (Exception e) {
            log.warn("获取创建人名称失败，ID：{}", standard.getCreatorId());
            dto.setCreatorName("未知");
        }
    }
    
    // 设置登记人名称
    if (standard.getRegistrarId() != null) {
        try {
            String registrarName = userService.getUserNameById(standard.getRegistrarId());
            dto.setRegistrarName(registrarName);
        } catch (Exception e) {
            log.warn("获取登记人名称失败，ID：{}", standard.getRegistrarId());
            dto.setRegistrarName("未知");
        }
    }
    
    // 处理明细项目 - 使用安全的方式获取数据
    if (standard.getItems() != null && !standard.getItems().isEmpty()) {
        List<StandardDetailResponseDTO.StandardItemInfoDTO> itemDTOs = 
            standard.getItems().stream()
                .map(item -> {
                    StandardDetailResponseDTO.StandardItemInfoDTO itemDTO = 
                        new StandardDetailResponseDTO.StandardItemInfoDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProjectCode(item.getProjectCode());
                    
                    // 安全获取项目信息
                    if (item.getProject() != null) {
                        itemDTO.setProjectName(item.getProject().getProjectName());
                        itemDTO.setProjectType(item.getProject().getProjectType());
                        itemDTO.setCategory(item.getProject().getCategory());
                    } else {
                        // 如果项目为空，尝试从数据库中获取
                        try {
                            SalaryProject project = salaryProjectRepository
                                .findByProjectCode(item.getProjectCode())
                                .orElse(null);
                            if (project != null) {
                                itemDTO.setProjectName(project.getProjectName());
                                itemDTO.setProjectType(project.getProjectType());
                                itemDTO.setCategory(project.getCategory());
                            }
                        } catch (Exception e) {
                            log.warn("获取项目信息失败，项目编码：{}", item.getProjectCode());
                        }
                    }
                    
                    itemDTO.setCalculationMethod(item.getCalculationMethod());
                    itemDTO.setAmount(item.getAmount());
                    itemDTO.setUnitPrice(item.getUnitPrice());
                    itemDTO.setRatio(item.getRatio());
                    // itemDTO.setFormula(item.getFormula());
                    itemDTO.setSortOrder(item.getSortOrder());
                    return itemDTO;
                })
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);
    }
    
    // 处理关联职位
    if (standard.getStandardPositions() != null && !standard.getStandardPositions().isEmpty()) {
        //批量获取机构名称
        Set<Long> orgIds = standard.getStandardPositions().stream()
        .filter(sp -> sp.getPosition() != null && sp.getPosition().getOrgId() != null)
        .map(sp -> sp.getPosition().getOrgId())
        .collect(Collectors.toSet());

        // 批量查询机构信息
    final Map<Long, Organization> orgMap = new HashMap<>();
    if (!orgIds.isEmpty()) {
        try {
              Map<Long, Organization> tempMap = orgRepository.findAllById(orgIds)
            .stream()
            .collect(Collectors.toMap(Organization::getOrgId, org -> org));
        orgMap.putAll(tempMap);
        } catch (Exception e) {
            log.warn("获取机构信息失败", e);
        }
    }

        List<StandardDetailResponseDTO.PositionInfoDTO> positionDTOs = 
            standard.getStandardPositions().stream()
                .map(sp -> {
                    StandardDetailResponseDTO.PositionInfoDTO positionDTO = 
                        new StandardDetailResponseDTO.PositionInfoDTO();
                    if (sp.getPosition() != null) {
                        positionDTO.setPosId(sp.getPosition().getPosId());
                        positionDTO.setPosCode(sp.getPosition().getPosCode());
                        positionDTO.setPosName(sp.getPosition().getPosName());
                        positionDTO.setOrgId(sp.getPosition().getOrgId());
                         // 设置机构名称（优先从缓存，其次从Position实体，最后查询）
                    String orgName = sp.getPosition().getOrgName();
                    if (orgName == null && sp.getPosition().getOrgId() != null) {
                        Organization org = orgMap.get(sp.getPosition().getOrgId());
                        orgName = org != null ? org.getOrgName() : "未知机构";
                    }
                    positionDTO.setOrgName(orgName != null ? orgName : "未知机构");
                    
                    positionDTO.setStatus(sp.getPosition().getStatus());
                    }
                    return positionDTO;
                })
                .collect(Collectors.toList());
        dto.setPositions(positionDTOs);
    }
    
    // 处理审核记录
    if (standard.getApprovals() != null && !standard.getApprovals().isEmpty()) {
        List<StandardDetailResponseDTO.ApprovalInfoDTO> approvalDTOs = 
            standard.getApprovals().stream()
                .map(this::convertApprovalToDTO)
                .collect(Collectors.toList());
        dto.setApprovals(approvalDTOs);
    }
    
    return dto;
}


/**
 * 转换审核记录为DTO
 */
private StandardDetailResponseDTO.ApprovalInfoDTO convertApprovalToDTO(StandardApproval approval) {
    StandardDetailResponseDTO.ApprovalInfoDTO dto = 
        new StandardDetailResponseDTO.ApprovalInfoDTO();
    dto.setId(approval.getId());
   dto.setStandardId(approval.getStandard().getStandardId());
    dto.setSubmitterId(approval.getSubmitterId());
    dto.setApproverId(approval.getApproverId());
    dto.setSubmitTime(approval.getSubmitTime());
    dto.setApprovalTime(approval.getApprovalTime());
    dto.setApprovalStatus(approval.getApprovalStatus());
    dto.setApprovalOpinion(approval.getApprovalOpinion());
    dto.setIsActive(approval.getIsActive());
    
    // 设置提交人名称
    dto.setSubmitterName(approval.getSubmitterName());
    
    // 设置审核人名称
    if (approval.getApproverId() != null) {
        try {
            String approverName = userService.getUserNameById(approval.getApproverId());
            dto.setApproverName(approverName);
        } catch (Exception e) {
            log.warn("获取审核人名称失败，ID：{}", approval.getApproverId());
            dto.setApproverName("未知");
        }
    }else{
        dto.setApproverName("待审核");
    }

    if (dto.getApprovalOpinion() == null) {
        dto.setApprovalOpinion("无");
    }
    
    return dto;
}

/* 获取所有标准主标 */
public List<Standard> getAllStandards() {
    log.info("获取所有薪酬标准");

    return standardRepository.findAll();
}

 /* 分页查询 */
    public Page<Standard>getStandardByPage(Pageable pageable){
        log.info("分页查询薪酬项目，页码：{}，页大小：{}",
           pageable.getPageNumber(), pageable.getPageSize());

        return standardRepository.findAll(pageable);
    }

    /**
     * 删除薪酬标准
     */
    @Transactional
    public void deleteStandard(Long standardId) {
        log.info("删除薪酬标准，ID：{}", standardId);
        
        try {
            // 1. 获取标准记录
            Standard standard = standardRepository.findById(standardId)
                    .orElseThrow(() -> new BusinessException(ResultCode.SALARY_STANDARD_NOT_EXIST, 
                            "薪酬标准不存在"));

            // 2. 检查状态（只有草稿和驳回状态可以删除）
            if (standard.getStatus() == 2 || standard.getStatus() == 3) {
                throw new BusinessException(ResultCode.SALARY_STANDARD_APPROVED, 
                        "已提交或已生效的标准不能删除");
            }

            // 3. 删除标准（级联删除关联数据）
            standardRepository.delete(standard);
            
            log.info("删除薪酬标准成功，ID：{}", standardId);

        } catch (BusinessException e) {
            log.error("删除薪酬标准业务异常：{}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("删除薪酬标准失败：", e);
            throw new BusinessException(ResultCode.ERROR, "删除薪酬标准失败：" + e.getMessage());
        }
    }

    /**
     * 获取审核记录
     */
/*     public List<StandardApproval> getApprovalHistory(Long standardId) {
        log.info("获取审核记录，标准ID：{}", standardId);
        
        return standardApprovalRepository.findByStandardIdOrderBySubmitTimeDesc(standardId);
    } */

    /**
     * 获取审核日志
     */
/*     public List<StandardApprovalLog> getApprovalLogs(Long approvalId) {
        log.info("获取审核日志，审核ID：{}", approvalId);
        
        return standardApprovalLogRepository.findByApprovalIdOrderByOperationTimeDesc(approvalId);
    }
 */
    /**
     * 获取指定机构的职位
     */
    public List<PositionVO> getPositionsByOrgId(Long orgId) {
        log.info("获取指定机构的职位，机构ID：{}", orgId);
        
        List<Position> positions = positionRepository.findByOrgId(orgId);
        
        return positions.stream()
                .map(position -> {
                    PositionVO vo = new PositionVO();
                    vo.setPosId(position.getPosId());
                    vo.setPosCode(position.getPosCode());
                    vo.setPosName(position.getPosName());
                    vo.setOrgId(position.getOrgId());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换实体为VO
     */
    private StandardVO convertToVO(Standard standard) {
        if (standard == null) {
            return null;
        }
        
        StandardVO vo = new StandardVO();
        BeanUtils.copyProperties(standard, vo);
        
        // 设置标准ID
        vo.setStandardId(standard.getStandardId());
        
        // 设置创建人名称
        if (standard.getCreatorId() != null) {
            try {
                String creatorName = userService.getUserNameById(standard.getCreatorId());
                vo.setCreatorName(creatorName);
            } catch (Exception e) {
                log.warn("获取创建人名称失败，ID：{}", standard.getCreatorId());
                vo.setCreatorName("未知");
            }
        }
        
        // 设置登记人名称
        if (standard.getRegistrarId() != null) {
            try {
                String registrarName = userService.getUserNameById(standard.getRegistrarId());
                vo.setRegistrarName(registrarName);
            } catch (Exception e) {
                log.warn("获取登记人名称失败，ID：{}", standard.getRegistrarId());
                vo.setRegistrarName("未知");
            }
        }
        
        // 设置明细项目
        List<StandardItem> items = standardItemRepository.findByStandardWithProject(standard);
        List<StandardItemVO> itemVOs = items.stream()
                .map(this::convertItemToVO)
                .collect(Collectors.toList());
        vo.setItems(itemVOs);
        
        // 设置关联职位
        List<StandardPosition> positions = standardPositionRepository.findByStandard(standard);
        List<PositionVO> positionVOs = positions.stream()
                .map(sp -> {
                    PositionVO positionVO = new PositionVO();
                    if (sp.getPosition() != null) {
                        positionVO.setPosId(sp.getPosition().getPosId());
                        positionVO.setPosCode(sp.getPosition().getPosCode());
                        positionVO.setPosName(sp.getPosition().getPosName());
                        positionVO.setOrgId(sp.getPosition().getOrgId());
                    }
                    return positionVO;
                })
                .collect(Collectors.toList());
        vo.setPositions(positionVOs);
        
        return vo;
    }
    
    /**
     * 转换明细项为VO
     */
    private StandardItemVO convertItemToVO(StandardItem item) {
        if (item == null) {
            return null;
        }
        
        return StandardItemVO.builder()
                .id(item.getId())
                .projectCode(item.getProjectCode())
                .projectName(item.getProject() != null ? item.getProject().getProjectName() : "")
                .projectType(item.getProject() != null ? item.getProject().getProjectType() : null)
                .category(item.getProject() != null ? item.getProject().getCategory() : "")
                .calculationMethod(item.getCalculationMethod())
                .amount(item.getAmount())
                .unitPrice(item.getUnitPrice())
                .ratio(item.getRatio())
                .sortOrder(item.getSortOrder())
                .build();
    }

   /**
 * 根据职位ID检查是否已有关联标准
 * 
 * 
 */

 @Transactional(readOnly = true)
public boolean checkPositionHasStandard(Long positionId, Long currentStandardId) {
    log.info("检查职位关联标准，positionId={}, currentStandardId={}", positionId, currentStandardId);
    
    try {
        // 如果 currentStandardId 为null，表示创建模式，检查是否被任何标准关联
        if (currentStandardId == null) {
            return standardPositionRepository.existsByPositionId(positionId);
        }
        
        // 编辑模式：检查是否被其他标准关联
        return standardPositionRepository.existsByPositionIdAndStandardIdNot(positionId, currentStandardId);
        
    } catch (Exception e) {
        log.error("检查职位关联标准失败，positionId={}", positionId, e);
        return false;
    }
}

@Transactional(readOnly = true)
public boolean checkPositionHasStandard(Long positionId) {
    return checkPositionHasStandard(positionId, null);
}

// 添加一个处理 String 参数的重载方法
@Transactional(readOnly = true)
public boolean checkPositionHasStandard(String positionIdStr) {
    System.out.println("开始检查职位关联标准（字符串参数）: positionIdStr=" + positionIdStr);
    
    try {
        Long positionId = Long.parseLong(positionIdStr);
        System.out.println("字符串转换成功: positionIdStr=" + positionIdStr + " -> positionId=" + positionId);
        return checkPositionHasStandard(positionId);
    } catch (NumberFormatException e) {
        System.out.println("职位ID格式错误: positionIdStr=" + positionIdStr + ", error=" + e.getMessage());
        log.error("职位ID格式错误: {}", positionIdStr, e);
        return false;
    }
}
    /**
     * 批量删除薪酬标准
     */
    @Transactional
    public void batchDeleteStandards(List<Long> standardIds) {
        log.info("批量删除薪酬标准，数量：{}", standardIds.size());
        
        for (Long standardId : standardIds) {
            try {
                deleteStandard(standardId);
            } catch (Exception e) {
                log.error("删除标准失败，ID：{}，错误：{}", standardId, e.getMessage());
                // 继续删除其他
            }
        }
        
        log.info("批量删除完成");
    }

     
}
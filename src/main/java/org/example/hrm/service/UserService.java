package org.example.hrm.service;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.dynamic.DynamicType.Builder.FieldDefinition.Optional;

import org.example.hrm.vo.UserArchiveVO;
import org.example.hrm.entity.Archive;
import org.example.hrm.entity.Organization;
import org.example.hrm.entity.User;
import org.example.hrm.repository.ArchiveRepository;
import org.example.hrm.repository.OrganizationRepository;
import org.example.hrm.repository.PositionRepository;
import org.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j  // 使用Lombok的日志注解
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ArchiveRepository archiveRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PositionRepository positionRepository;
    // @Autowired
    // private RoleService roleService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== 尝试加载用户: {}", username);
        
        User user = userRepository.findByUserCode(username)
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });
        
        log.info("找到用户: {}, ID: {}, 状态: {}, 密码: {}", 
            user.getUserCode(), user.getUserId(), user.getStatus(), user.getPassword());
        log.info("密码长度: {}, 密码是否以$2a$开头: {}", 
            user.getPassword().length(), user.getPassword().startsWith("$2a$"));
        
        if (user.getStatus() == 0) {
            log.error("用户已离职: {}", username);
            throw new UsernameNotFoundException("用户已离职");
        }
        
        // 创建UserDetails对象
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUserCode(),
                user.getPassword(),  // 注意：这里使用数据库中的密码
                getAuthorities(user)
        );
        
        log.info("用户加载成功: {}", username);
        return userDetails;
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        log.info("为用户 {} 分配权限，角色类型: {}", user.getUserCode(), user.getRoleType());
        
        // 根据role_type分配角色
        if (user.getRoleType() != null) {
            switch (user.getRoleType()) {
                case 1:
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    log.info("分配角色: ROLE_MANAGER");
                    break;
                case 2:
                    authorities.add(new SimpleGrantedAuthority("ROLE_HR_MANAGER"));
                    log.info("分配角色: ROLE_HR_MANAGER");
                    break;
                case 3:
                    authorities.add(new SimpleGrantedAuthority("ROLE_FINANCE_MANAGER"));
                    log.info("分配角色: ROLE_FINANCE_MANAGER");
                    break;
                case 4:
                    authorities.add(new SimpleGrantedAuthority("ROLE_HR_SPECIALIST"));
                    log.info("分配角色: ROLE_HR_SPECIALIST");
                    break;
                case 5:
                    authorities.add(new SimpleGrantedAuthority("ROLE_FINANCE_SPECIALIST"));
                    log.info("分配角色: ROLE_FINANCE_SPECIALIST");
                    break;
                case 6:
                    authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
                    log.info("分配角色: ROLE_EMPLOYEE");
                    break;
                default:
                    log.warn("未知角色类型: {}", user.getRoleType());
            }
        } else {
            log.warn("用户 {} 的角色类型为空", user.getUserCode());
        }
        
        return authorities;
    }
    
    public User findByUserCode(String userCode) {
        log.info("查询用户: {}", userCode);
        return userRepository.findByUserCode(userCode).orElse(null);
    }
    
    public User register(User user) {
        log.info("注册新用户: {}", user.getUserCode());
        
        if (userRepository.findByUserCode(user.getUserCode()).isPresent()) {
            throw new RuntimeException("用户已存在");
        }
        
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            log.info("加密密码: {} -> {}", user.getPassword(), encodedPassword);
            user.setPassword(encodedPassword);
        }
        
        if (user.getRoleType() == null) {
            user.setRoleType(6);
            log.info("设置默认角色: 普通员工");
        }
        
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUserCode());
        return savedUser;
    }
    
    public List<User> findAll() {
      return userRepository.findAll();
    }

    /**
     * 根据用户ID获取用户名
     */
    public String getUserNameById(Long userId) {
        if (userId == null) {
            log.warn("获取用户名失败：用户ID为空");
            return "未知";
        }
        
        try {
            return userRepository.findById(userId)
                    .map(User::getUserName)
                    .orElse("未知");
        } catch (Exception e) {
            log.error("获取用户名失败，用户ID：{}", userId, e);
            return "未知";
        }
    }

    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        return userRepository.findById(userId).orElse(null);
    }

    /**
 * 批量获取用户名称
 */
public List<Map<String, Object>> batchGetUserNames(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
        return new ArrayList<>();
    }
    
    try {
        // 这里根据你的UserRepository实现具体查询逻辑
        // 示例：查询用户表，返回用户ID和名称
        return userRepository.findUserNamesByIds(userIds);
    } catch (Exception e) {
        log.error("批量获取用户名称失败", e);
        return new ArrayList<>();
    }
}

 /**
     * 获取当前登录用户的完整信息（用户表+档案表）
     */
    public UserArchiveVO getCurrentUserInfo(Long userId) {
        log.info("获取用户完整信息，用户ID: {}", userId);
        
        try {
            // 1. 查询用户信息
            User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户不存在，用户ID: {}", userId);
                    return new RuntimeException("用户不存在");
                });
            
            log.info("找到用户: {}, 工号: {}, 档案ID: {}", 
                user.getUserName(), user.getUserCode(), user.getArchiveId());
            
            // 2. 创建返回对象
            UserArchiveVO vo = new UserArchiveVO();
            
            // 3. 填充用户表信息
            copyUserInfoToVO(user, vo);
            
            // 4. 如果有档案ID，查询档案信息
            if (user.getArchiveId() != null) {
                java.util.Optional<Archive> archiveOpt = archiveRepository.findById(user.getArchiveId());
                if (archiveOpt.isPresent()) {
                    Archive archive = archiveOpt.get();
                    log.info("找到关联档案: {}, 姓名: {}", archive.getArcCode(), archive.getName());
                    copyArchiveInfoToVO(archive, vo);
                } else {
                    log.warn("用户关联的档案不存在，档案ID: {}", user.getArchiveId());
                }
            } else {
                log.info("用户未关联档案");
            }
            
            // 5. 获取机构名称
            setOrganizationInfo(vo);
            
            log.info("用户信息获取成功: {}", user.getUserName());
            return vo;
            
        } catch (Exception e) {
            log.error("获取用户信息失败，userId: {}", userId, e);
            throw new RuntimeException("获取用户信息失败");
        }
    }

     /**
     * 根据工号获取用户信息
     */
    public UserArchiveVO getUserByCode(String userCode) {
        log.info("根据工号获取用户信息: {}", userCode);
        
        User user = userRepository.findByUserCode(userCode)
            .orElseThrow(() -> {
                log.error("用户不存在，工号: {}", userCode);
                return new RuntimeException("用户不存在");
            });
        
        return getCurrentUserInfo(user.getUserId());
    }

    /**
     * 复制用户信息到VO
     */
    private void copyUserInfoToVO(User user, UserArchiveVO vo) {
        vo.setUserId(user.getUserId());
        vo.setUserCode(user.getUserCode());
        vo.setUserName(user.getUserName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setOrgId(user.getOrgId());
        vo.setPosId(user.getPosId());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoleType(user.getRoleType());
        vo.setEntryDate(user.getEntryDate());
        vo.setLeaveDate(user.getLeaveDate());
        vo.setArchiveId(user.getArchiveId());
    }
    
    /**
     * 复制档案信息到VO
     */
    private void copyArchiveInfoToVO(Archive archive, UserArchiveVO vo) {
        vo.setArcId(archive.getArcId());
        vo.setArcCode(archive.getArcCode());
        vo.setName(archive.getName());
        vo.setSex(archive.getSex());
        vo.setIdCard(archive.getIdCard());
        vo.setFirstOrgId(archive.getFirstOrgId());
        vo.setSecondOrgId(archive.getSecondOrgId());
        vo.setThirdOrgId(archive.getThirdOrgId());
        vo.setPositionName(archive.getPositionName());
        vo.setTitle(archive.getTitle());
        vo.setSalaryStandard(archive.getSalaryStandard());
        vo.setBirDate(archive.getBirDate());
        vo.setNationality(archive.getNationality());
        vo.setQualification(archive.getQualification());
        vo.setPhotoPath(archive.getPhotoPath());
        vo.setTelephone(archive.getTelephone());
        vo.setAddress(archive.getAddress());
        vo.setZipCode(archive.getZipCode());
        vo.setCountry(archive.getCountry());
        vo.setBirAddress(archive.getBirAddress());
        vo.setBelief(archive.getBelief());
        vo.setIdentity(archive.getIdentity());
        vo.setMajor(archive.getMajor());
        vo.setArchiveStatus(archive.getStatus());
        vo.setReason(archive.getReason());
        vo.setResubmitCount(archive.getResubmitCount());
    }
    
    /**
     * 设置机构信息
     */
    private void setOrganizationInfo(UserArchiveVO vo) {
        // 获取档案中的机构信息
        if (vo.getFirstOrgId() != null) {
            organizationRepository.findById(vo.getFirstOrgId())
                .ifPresent(org -> vo.setFirstOrgName(org.getOrgName()));
        }
        if (vo.getSecondOrgId() != null) {
            organizationRepository.findById(vo.getSecondOrgId())
                .ifPresent(org -> vo.setSecondOrgName(org.getOrgName()));
        }
        if (vo.getThirdOrgId() != null) {
            organizationRepository.findById(vo.getThirdOrgId())
                .ifPresent(org -> vo.setThirdOrgName(org.getOrgName()));
        }
        
        // 获取用户表中的机构信息
        if (vo.getOrgId() != null) {
            organizationRepository.findById(vo.getOrgId())
                .ifPresent(org -> vo.setOrgName(org.getOrgName()));
        }
        
        // 获取职位信息
        if (vo.getPosId() != null) {
            positionRepository.findById(vo.getPosId())
                .ifPresent(pos -> {
                    if (vo.getPositionName() == null) {
                        vo.setPositionName(pos.getPosName());
                    }
                });
        }
    }
    
    /**
     * 根据用户ID获取用户和档案信息
     */
    public UserArchiveVO getUserArchiveById(Long userId) {
        return getCurrentUserInfo(userId);
    }
    

/**
 * 根据机构ID获取员工列表
 */
public List<UserArchiveVO> getEmployeesByOrgId(Long orgId) {
    try {
        log.info("根据机构ID {} 查询员工列表", orgId);
        
        // 1. 查询机构信息，确定机构层级
        java.util.Optional<Organization> orgOpt = organizationRepository.findById(orgId);
        if (!orgOpt.isPresent()) {
            log.warn("机构 {} 不存在", orgId);
            return new ArrayList<>();
        }
        
        Organization org = orgOpt.get();
        Integer orgLevel = org.getOrgLevel();
        
        // 2. 根据机构层级查询档案
        List<Archive> archives;
        if (orgLevel == 1) {
            // 一级机构：查询所有下属机构的档案
            archives = archiveRepository.findByFirstOrgId(orgId);
        } else if (orgLevel == 2) {
            // 二级机构：查询所有下属三级机构的档案
            archives = archiveRepository.findBySecondOrgId(orgId);
        } else {
            // 三级机构：直接查询该机构的档案
            archives = archiveRepository.findByThirdOrgId(orgId);
        }
        
        if (archives == null || archives.isEmpty()) {
            log.info("机构 {} 下没有员工档案", orgId);
            return new ArrayList<>();
        }
        
        // 3. 过滤只显示已通过的档案（status=2）
        List<Archive> activeArchives = archives.stream()
            .filter(archive -> archive.getStatus() == 2) // 状态2表示已通过
            .collect(Collectors.toList());
        
        if (activeArchives.isEmpty()) {
            log.info("机构 {} 下没有已通过的员工档案", orgId);
            return new ArrayList<>();
        }
        
        // 4. 转换为VO
        List<UserArchiveVO> result = new ArrayList<>();
        for (Archive archive : activeArchives) {
            try {
                UserArchiveVO vo = convertArchiveToVO(archive);
                if (vo != null) {
                    result.add(vo);
                }
            } catch (Exception e) {
                log.error("转换档案 {} 失败", archive.getArcCode(), e);
            }
        }
        
        log.info("机构 {} ({}级) 下找到 {} 名已通过的员工", 
                 org.getOrgName(), orgLevel, result.size());
        return result;
        
    } catch (Exception e) {
        log.error("查询机构员工列表失败", e);
        throw new RuntimeException("查询机构员工列表失败: " + e.getMessage());
    }
}

/**
 * 根据机构ID获取员工数量
 */
public Integer getEmployeeCountByOrgId(Long orgId) {
    try {
        log.info("统计机构 {} 的员工数量", orgId);
        
        // 查询机构信息
        java.util.Optional<Organization> orgOpt = organizationRepository.findById(orgId);
        if (!orgOpt.isPresent()) {
            return 0;
        }
        
        Organization org = orgOpt.get();
        Integer orgLevel = org.getOrgLevel();
        
        // 根据机构层级查询档案
        List<Archive> archives;
        if (orgLevel == 1) {
            archives = archiveRepository.findByFirstOrgId(orgId);
        } else if (orgLevel == 2) {
            archives = archiveRepository.findBySecondOrgId(orgId);
        } else {
            archives = archiveRepository.findByThirdOrgId(orgId);
        }
        
        if (archives == null) {
            return 0;
        }
        
        // 统计已通过的档案数量
        long count = archives.stream()
            .filter(archive -> archive.getStatus() == 2) // 状态2表示已通过
            .count();
        
        return (int) count;
        
    } catch (Exception e) {
        log.error("统计机构员工数量失败", e);
        return 0;
    }
}

/**
 * 将Archive转换为UserArchiveVO
 */
private UserArchiveVO convertArchiveToVO(Archive archive) {
    if (archive == null) {
        return null;
    }
    
    try {

        System.out.println("=== 开始转换Archive为VO ===");
        System.out.println("档案ID: " + archive.getArcId() + ", 姓名: " + archive.getName() + ", 状态: " + archive.getStatus());
        UserArchiveVO vo = new UserArchiveVO();
        
        // 设置档案基本信息
        vo.setArcId(archive.getArcId());
        vo.setArcCode(archive.getArcCode());
        vo.setName(archive.getName());
        vo.setSex(archive.getSex());
        vo.setIdCard(archive.getIdCard());
        vo.setFirstOrgId(archive.getFirstOrgId());
        vo.setSecondOrgId(archive.getSecondOrgId());
        vo.setThirdOrgId(archive.getThirdOrgId());
        vo.setPositionName(archive.getPositionName());
        vo.setTitle(archive.getTitle());
        vo.setSalaryStandard(archive.getSalaryStandard());
        vo.setBirDate(archive.getBirDate());
        vo.setNationality(archive.getNationality());
        vo.setQualification(archive.getQualification());
        vo.setPhotoPath(archive.getPhotoPath());
        vo.setTelephone(archive.getTelephone());
        vo.setAddress(archive.getAddress());
        vo.setZipCode(archive.getZipCode());
        vo.setCountry(archive.getCountry());
        vo.setBirAddress(archive.getBirAddress());
        vo.setBelief(archive.getBelief());
        vo.setIdentity(archive.getIdentity());
        vo.setMajor(archive.getMajor());
        vo.setArchiveStatus(archive.getStatus());
        vo.setReason(archive.getReason());
        vo.setResubmitCount(archive.getResubmitCount());
        
        log.info("档案基本信息设置完成 - ArcCode: {}, Name: {}, ThirdOrgId: {}", 
                 vo.getArcCode(), vo.getName(), vo.getThirdOrgId());

        // 设置机构名称
        setArchiveOrganizationInfo(archive, vo);
        
        // 查找关联的用户信息
        java.util.Optional<User> userOpt = userRepository.findByArchiveId(archive.getArcId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            vo.setUserId(user.getUserId());
            vo.setUserCode(user.getUserCode());
            vo.setUserName(user.getUserName());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setOrgId(user.getOrgId());
            vo.setPosId(user.getPosId());
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());
            vo.setUpdateTime(user.getUpdateTime());
            vo.setRoleType(user.getRoleType());
            vo.setEntryDate(user.getEntryDate());
            vo.setLeaveDate(user.getLeaveDate());
            vo.setArchiveId(user.getArchiveId());

             log.info("用户信息设置完成 - UserCode: {}, UserName: {}", 
                     vo.getUserCode(), vo.getUserName());
            
            // 如果没有机构信息，使用用户表中的机构
            if (vo.getOrgId() != null && vo.getOrgName() == null) {
                organizationRepository.findById(vo.getOrgId())
                    .ifPresent(org -> vo.setOrgName(org.getOrgName()));
            }
        } else {
            vo.setStatus(0); // 0表示未关联用户
        }
        
        return vo;
        
    } catch (Exception e) {
        log.error("转换Archive为VO失败", e);
        return null;
    }
}

/**
 * 为档案设置机构信息
 */
private void setArchiveOrganizationInfo(Archive archive, UserArchiveVO vo) {
    // 设置一级机构名称
    if (archive.getFirstOrgId() != null) {
        organizationRepository.findById(archive.getFirstOrgId())
            .ifPresent(org -> vo.setFirstOrgName(org.getOrgName()));
    }
    
    // 设置二级机构名称
    if (archive.getSecondOrgId() != null) {
        organizationRepository.findById(archive.getSecondOrgId())
            .ifPresent(org -> vo.setSecondOrgName(org.getOrgName()));
    }
    
    // 设置三级机构名称
    if (archive.getThirdOrgId() != null) {
        organizationRepository.findById(archive.getThirdOrgId())
            .ifPresent(org -> {
                vo.setThirdOrgName(org.getOrgName());
                vo.setOrgId(org.getOrgId()); // 设置主机构ID
                vo.setOrgName(org.getOrgName()); // 设置主机构名称
            });
    }
}
}


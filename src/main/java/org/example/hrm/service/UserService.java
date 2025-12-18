package org.example.hrm.service;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.dynamic.DynamicType.Builder.FieldDefinition.Optional;

import org.example.hrm.vo.UserArchiveVO;
import org.example.hrm.entity.Archive;
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
    
}
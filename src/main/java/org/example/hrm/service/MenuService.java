package org.example.hrm.service;

import lombok.extern.slf4j.Slf4j;
import org.example.hrm.entity.SystemMenu;
import org.example.hrm.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class MenuService {
    
    @Autowired
    private MenuRepository menuRepository;
    
    public List<SystemMenu> getMenusByRoleType(Integer roleType) {
        log.info("根据角色类型获取菜单: {}", roleType);
        
        // 临时解决方案：先返回空列表，确保登录能成功
        // 这样可以排除菜单查询的问题
        List<SystemMenu> result = new ArrayList<>();
        
        try {
            // 简单逻辑：返回所有菜单，或者根据角色类型过滤
            if (roleType != null) {
                if (roleType == 1) { // 系统管理员
                    result = menuRepository.findAll();
                } else {
                    // 其他角色返回空列表或有限菜单
                    result = menuRepository.findByStatus(1);
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取菜单时出错", e);
            result = new ArrayList<>();
        }
        
        log.info("返回菜单数量: {}", result.size());
        return result;
    }
    
    public List<SystemMenu> getAllMenus() {
        return menuRepository.findAll();
    }
    
    public SystemMenu save(SystemMenu menu) {
        return menuRepository.save(menu);
    }
}
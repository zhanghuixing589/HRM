package org.example.hrm.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.hrm.service.UserService;
import org.example.hrm.vo.UserArchiveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 根据用户ID获取完整信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        try {
            log.info("根据用户ID获取信息: {}", userId);
            System.out.println("根据用户ID获取信息：" + userId);
            UserArchiveVO userInfo = userService.getUserArchiveById(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", userInfo);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取用户信息失败，userId: {}", userId, e);
            System.out.println("获取用户信息失败，userId: " + userId + ", 错误信息: " + e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
            
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 根据工号获取用户信息
     */
    @GetMapping("/code/{userCode}")
    public ResponseEntity<Map<String, Object>> getUserByCode(@PathVariable String userCode) {
        try {
            log.info("根据工号获取用户信息: {}", userCode);
            System.out.println("根据工号获取用户信息：" + userCode);
            UserArchiveVO userInfo = userService.getUserByCode(userCode);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", userInfo);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取用户信息失败，userCode: {}", userCode, e);
            System.out.println("获取用户信息失败，userCode: " + userCode + ", 错误信息: " + e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
            
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            log.info("获取当前登录用户信息");
            System.out.println("获取当前登录用户信息");
            
            // 从SecurityContext获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("用户未登录");
            }
            
            String username = authentication.getName();
            log.info("当前登录用户名: {}", username);
            
            // 根据用户名查询用户ID
            org.example.hrm.entity.User user = userService.findByUserCode(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 获取完整用户信息
            UserArchiveVO userInfo = userService.getCurrentUserInfo(user.getUserId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", userInfo);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            System.out.println("获取当前用户信息失败，错误信息: " + e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
            
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取用户基本信息（用于测试）
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        try {
            log.info("获取用户基本信息");
            
            // 从SecurityContext获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("用户未登录");
            }
            
            String username = authentication.getName();
            log.info("当前登录用户名: {}", username);
            
            // 根据用户名查询用户
            org.example.hrm.entity.User user = userService.findByUserCode(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 返回基本信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("userCode", user.getUserCode());
            userInfo.put("userName", user.getUserName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("roleType", user.getRoleType());
            userInfo.put("status", user.getStatus());
            userInfo.put("archiveId", user.getArchiveId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", userInfo);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取用户基本信息失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
            
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/test")
public ResponseEntity<String> test() {
    log.info("=== 测试端点被调用 ===");
    return ResponseEntity.ok("Test endpoint works!");
}



/**
 * 根据机构ID获取员工列表
 */
@GetMapping("/org/{orgId}/employees")
public ResponseEntity<Map<String, Object>> getEmployeesByOrgId(@PathVariable Long orgId) {
    try {
        log.info("根据机构ID获取员工列表: {}", orgId);
        System.out.println("根据机构ID获取员工列表：" + orgId);
        
        // 调用Service获取员工列表
        List<UserArchiveVO> employees = userService.getEmployeesByOrgId(orgId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("success", true);
        result.put("message", "success");
        result.put("data", employees);
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        log.error("获取机构员工列表失败，orgId: {}", orgId, e);
        System.out.println("获取机构员工列表失败，orgId: " + orgId + ", 错误信息: " + e.getMessage());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("success", false);
        result.put("message", e.getMessage());
        result.put("data", null);
        
        return ResponseEntity.status(500).body(result);
    }
}

/**
 * 根据机构ID获取员工数量
 */
@GetMapping("/org/{orgId}/employee-count")
public ResponseEntity<Map<String, Object>> getEmployeeCountByOrgId(@PathVariable Long orgId) {
    try {
        log.info("根据机构ID获取员工数量: {}", orgId);
        System.out.println("根据机构ID获取员工数量：" + orgId);
        
        // 调用Service获取员工数量
        Integer count = userService.getEmployeeCountByOrgId(orgId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("success", true);
        result.put("message", "success");
        result.put("data", count != null ? count : 0);
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        log.error("获取机构员工数量失败，orgId: {}", orgId, e);
        System.out.println("获取机构员工数量失败，orgId: " + orgId + ", 错误信息: " + e.getMessage());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("success", false);
        result.put("message", e.getMessage());
        result.put("data", 0);
        
        return ResponseEntity.status(500).body(result);
    }
}
}
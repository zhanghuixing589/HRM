package org.example.hrm.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.hrm.common.Result;
import org.example.hrm.entity.User;
import org.example.hrm.util.JwtTokenUtil;
import org.example.hrm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 验证用户名密码
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = userService.findByUserCode(loginRequest.getUsername());
            if (user == null) {
                return ResponseEntity.badRequest().body(Result.error("用户不存在"));
            }
            
            if (user.getStatus() == 0) {
                return ResponseEntity.badRequest().body(Result.error("用户已离职"));
            }

            if (user.getStatus() == 2) {
                return ResponseEntity.badRequest().body(Result.error("用户已禁用"));
            }
            
            // 生成JWT token
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            final String token = jwtTokenUtil.generateToken(userDetails, user.getRoleType(), user.getUserId());
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", jwtTokenUtil.getTokenPrefix() + token);
            result.put("user", user);
            result.put("roleType", user.getRoleType());
            result.put("redirectUrl", getRedirectUrl(user.getRoleType()));

            return ResponseEntity.ok(Result.success(result));
            
        } catch (Exception e) {
            e.printStackTrace();  // 确保控制台也打印
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
    
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith(jwtTokenUtil.getTokenPrefix())) {
                token = token.substring(jwtTokenUtil.getTokenPrefix().length());
            }
            
            String username = jwtTokenUtil.getUsernameFromToken(token);
            User user = userService.findByUserCode(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Result.error("用户不存在"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("roleType", user.getRoleType());
            
            return ResponseEntity.ok(Result.success(result));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error("获取用户信息失败"));
        }
    }
    
    // 根据角色类型返回跳转路径
    private String getRedirectUrl(Integer roleType) {
        switch (roleType) {
            case 1: // 系统管理员
                return "/admin/user-role";
            case 2: // 人事经理
            case 4: // 人事专员
                return "/hr/archive";
            case 3: // 薪酬经理
            case 5: // 薪酬专员
                return "/salary/manage";
            case 6: // 普通员工
                return "/employee/dashboard";
            default:
                return "/";
        }
    }
    
    // 登录请求类
    public static class LoginRequest {
        private String username;
        private String password;
        
        // getters and setters
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}

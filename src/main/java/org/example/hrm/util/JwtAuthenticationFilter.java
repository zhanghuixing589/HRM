package org.example.hrm.util;

import org.example.hrm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserService userService;
    
    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain) throws ServletException, IOException {
    
    final String requestTokenHeader = request.getHeader(jwtTokenUtil.getHeader());
    
    String username = null;
    String jwtToken = null;
    Long userId = null; // 新增：存储用户ID
    
    // 从请求头中获取token
    if (requestTokenHeader != null && requestTokenHeader.startsWith(jwtTokenUtil.getTokenPrefix())) {
        jwtToken = requestTokenHeader.substring(jwtTokenUtil.getTokenPrefix().length());
        try {
            username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            // 新增：从Token中获取用户ID
            userId = jwtTokenUtil.getUserIdFromToken(jwtToken);
            // logger.info("从Token中解析到用户: {}, 用户ID: {}", username, userId);
        } catch (Exception e) {
            logger.error("JWT Token解析失败", e);
        }
    } else {
        logger.warn("JWT Token不存在或格式不正确");
    }
    
    // 验证token
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userService.loadUserByUsername(username);
        
        if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 关键：设置userId到请求属性中
            if (userId != null) {
                request.setAttribute("userId", userId);
                // logger.info("已将用户ID {} 设置到请求属性", userId);
            }
        }
    }
    chain.doFilter(request, response);
}
}

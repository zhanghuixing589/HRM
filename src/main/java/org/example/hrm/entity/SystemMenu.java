package org.example.hrm.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_menu")
@Data
public class SystemMenu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;
    
    @Column(name = "menu_code", unique = true, nullable = false, length = 50)
    private String menuCode;
    
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;
    
    @Column(name = "menu_url", length = 200)
    private String menuUrl;
    
    @Column(name = "parent_id")
    private Long parentId = 0L;
    
    @Column(name = "menu_type")
    private Integer menuType = 1; // 1-菜单，2-按钮
    
    @Column(name = "icon", length = 100)
    private String icon;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "status")
    private Integer status = 1; // 1-启用，0-禁用
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
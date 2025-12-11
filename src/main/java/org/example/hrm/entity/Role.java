package org.example.hrm.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "role")
@Data
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;
    
    @Column(name = "role_code", unique = true, nullable = false, length = 50)
    private String roleCode;
    
    @Column(name = "role_name", length = 100)
    private String roleName;
    
    @Column(name = "role_type", nullable = false)
    private Integer roleType;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "remark", length = 200)
    private String remark;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 修改为LAZY加载
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_menu",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private List<SystemMenu> menus;
}
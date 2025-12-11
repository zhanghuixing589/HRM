package org.example.hrm.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "user_code", unique = true, nullable = false, length = 50)
    private String userCode; // 工号
    
    @Column(name = "user_name", length = 100)
    private String userName;
    
    @Column(name = "password", nullable = false, length = 100)
    private String password;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "org_id")
    private Long orgId;
    
    @Column(name = "pos_id")
    private Long posId;
    
    @Column(name = "status")
    private Integer status = 1; // 1-在职，0-离职
    
    @Column(name = "role_type")
    private Integer roleType; // 角色类型（1-系统管理员，2-人事经理...）
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 关联角色（多对多）
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Role role;
}
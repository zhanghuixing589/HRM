package org.example.hrm.entity;

import lombok.*;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Integer status = 1; // 1-在职，0-离职，2-禁用
    
    @Column(name = "role_type")
    private Integer roleType; // 角色类型（1-系统管理员，2-人事经理...）

    // 添加入职时间和离职时间
    @Column(name = "entry_date")
    private LocalDate entryDate; // 入职日期
    
    @Column(name = "leave_date")
    private LocalDate leaveDate; // 离职日期（离职状态时填写）
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    //计算属性，不存储在数据库中
    @Transient
    private String orgName; 

    @Transient
    private String positionName; // 职位名称
    
    @Transient
    private String roleName; // 角色名称
    
    @Transient
    private String statusName; // 状态名称
    
    @Transient
    private Integer workYears; // 工作年限

     /**
     * JPA 保存前的回调方法
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;

        //如果没有设置入职日期，默认设置为创建时间
        if (entryDate == null) {
            entryDate = now.toLocalDate();
        }

        //设置默认状态为在职
        if (status == null) {
            status = 1; // 在职
        }

    }

    /**
     * JPA 更新前的回调方法
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();

        //如果没有设置离职日期且状态为离职，则设置离职日期为当前日期
        if (status != null && status == 0 && leaveDate == null) {
            leaveDate = LocalDate.now();
        }
    }

    /* *
    *判断是否为在职状态
     */
    public boolean isActive() {
        return this.status != null && this.status == 1;
    }

    //是否离职
    public boolean isResigned() {
        return this.status != null && this.status == 0;
    }

    //是否警用
    public boolean isDisabled() {
        return this.status != null && this.status == 2;
    }

    //计算工作年限
    public Integer calculateWorkYears() {
        if (entryDate == null) {
            return 0;
        }

        LocalDate now = LocalDate.now();
        int  years = now.getYear() - entryDate.getYear();

        //如果当前日期在入职日期之前或相同但当前日期小于入职日，减去一年
        if (now.getMonthValue() < entryDate.getMonthValue() || (now.getMonthValue() == entryDate.getMonthValue() 
             && now.getDayOfMonth() < entryDate.getDayOfMonth())) {
            years--;
        }

        return Math.max(0, years);

    }

    /**
     * 判断员工是否可以参与薪酬计算
     * 只有在职员工可以计算薪酬
     */
    public boolean canCalculateSalary(){
        //只有在职员工可以计算
        if (!isActive()) {
            return false;
        }

        //必须有入职日期
        if (entryDate == null) {
            return false;
        }

        return true;
    }

    /* 判断员工是否在试用期内 - 薪酬计算 */
    public boolean isInProbation(LocalDate salaryMonth){
        if (entryDate == null || salaryMonth == null) {
            return false;
        }

        //试用期通常为入职后3个月
        LocalDate probationEndDate = entryDate.plusMonths(3);

        //如果使用期结束日期在当前月份或之前，则不在试用期
        return salaryMonth.isBefore(probationEndDate) || 
        salaryMonth.getMonthValue() == probationEndDate.getMonthValue() && salaryMonth.getYear() == probationEndDate.getYear();
    }

    /* 判断员工是否在试用期 - 相对于当前时间 */
    public boolean isInProbation(){
        return isInProbation(LocalDate.now());
    }

    /* 计算员工在指定月份的工作天数 */
    public int calculateWorkDaysInMonth(LocalDate salaryMonth){
        if (salaryMonth == null || entryDate == null) {
            return 0;
        }

        LocalDate monthStart = salaryMonth.withDayOfMonth(1);
        LocalDate monthEnd = salaryMonth.withDayOfMonth(salaryMonth.lengthOfMonth());

        //如果入职日期在本月之后，工作天数为0
        if (entryDate.isAfter(monthEnd)) {
            return 0;
        }

        //如果员工在该月之前入职，从月初开始计算
        LocalDate startDate = entryDate.isAfter(monthStart) ? entryDate : monthStart;

        //如果员工已离职且在该月之前离职，返回0
        if(isResigned() && leaveDate != null && leaveDate.isBefore(monthStart)){
            return 0;
        }

        //如果员工已离职且在该月内离职，计算到离职日期
        LocalDate endDate = monthEnd;
        if (isResigned() && leaveDate != null && leaveDate.isBefore(monthEnd)) {
            endDate = leaveDate;
        }

        //计算工作天数（排除周末）
        int workDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek().getValue() <= 5) {
                //周一到周五为工作日
                workDays++;
                
            }
            date = date.plusDays(1);
        }

        return workDays;

    }

}
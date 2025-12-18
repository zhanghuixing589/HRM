package org.example.hrm.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "standard_item")
@Data
@Builder
public class StandardItem implements Serializable {
    private static final long serialVersionUID = 1L;

 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id")
    private Standard standard;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", insertable = false, updatable = false)
    private SalaryProject project;

    @Column(name = "project_code", nullable = false)
    private String projectCode;

    
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

     @Column(name = "ratio", precision = 5, scale = 4)
    private BigDecimal ratio;
    
    @Column(name = "calculation_method")
    private String calculationMethod; // fixed/percentage/formula
    
    @Column(name = "sort_order")
    private Integer sortOrder;

    public StandardItem(){}
    // ...existing code...
public StandardItem(Long id, Standard standard, SalaryProject project, String projectCode,
                    BigDecimal amount, BigDecimal unitPrice, BigDecimal ratio,
                    String calculationMethod, Integer sortOrder) {
    this.id = id;
    this.standard = standard;
    this.project = project;
    this.projectCode = projectCode;
    this.amount = amount;
    this.unitPrice = unitPrice;
    this.ratio = ratio;
    this.calculationMethod = calculationMethod;
    this.sortOrder = sortOrder;
}

 // 重写 equals 和 hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardItem that = (StandardItem) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
}
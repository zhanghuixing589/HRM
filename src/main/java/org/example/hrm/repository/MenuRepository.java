package org.example.hrm.repository;

import org.example.hrm.entity.SystemMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<SystemMenu, Long> {
    
    List<SystemMenu> findByParentId(Long parentId);
    
    List<SystemMenu> findByStatus(Integer status);
}

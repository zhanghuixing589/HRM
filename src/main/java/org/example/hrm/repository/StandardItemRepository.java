package org.example.hrm.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.example.hrm.dto.StandardItemDTO;
import org.example.hrm.dto.StandardItemProjectionDTO;
import org.example.hrm.dto.StandardDetailResponseDTO;
import org.example.hrm.dto.StandardDetailResponseDTO.StandardItemInfoDTO;
import org.example.hrm.entity.Standard;
import org.example.hrm.entity.StandardItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardItemRepository extends JpaRepository<StandardItem, Long>{

  // 修改：使用现有的 StandardItemDTO，确保字段匹配
    @Query("SELECT new org.example.hrm.dto.StandardItemDTO(" +
           "si.projectCode, si.amount, si.unitPrice, si.ratio, " +
           "si.calculationMethod, si.sortOrder) " +
           "FROM StandardItem si " +
           "WHERE si.standard.standardId = :standardId " +
           "ORDER BY si.sortOrder")
    List<StandardItemInfoDTO> findItemDTOsByStandardId(@Param("standardId") Long standardId);
    

    
    List<StandardItem> findByStandard(Standard standard);

     /**
     * 根据标准ID强制删除（绕过Hibernate缓存，立即执行）
     */
    @Modifying
    @Query(value = "DELETE FROM standard_item WHERE standard_id = :standardId", nativeQuery = true)
    void deleteByStandardIdNative(@Param("standardId") Long standardId);

    @Modifying
    @Transactional
    void deleteByStandard(Standard standard);

   @Query("SELECT si FROM StandardItem si JOIN FETCH si.project WHERE si.standard = :standard")
    List<StandardItem> findByStandardWithProject(@Param("standard") Standard standard);


}

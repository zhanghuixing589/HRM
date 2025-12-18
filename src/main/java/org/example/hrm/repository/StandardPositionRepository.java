package org.example.hrm.repository;

import java.util.List;

import org.example.hrm.dto.OccupationDetailDTO;
import org.example.hrm.dto.StandardDetailResponseDTO;
import org.example.hrm.dto.StandardDetailResponseDTO.PositionInfoDTO;
import org.example.hrm.entity.Standard;
import org.example.hrm.entity.StandardPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardPositionRepository extends JpaRepository<StandardPosition,Long> {

   /**
     * 根据标准ID强制删除（绕过Hibernate缓存，立即执行）
     */
    @Modifying
    @Query(value = "DELETE FROM standard_position WHERE standard_id = :standardId", nativeQuery = true)
    void deleteByStandardIdNative(@Param("standardId") Long standardId);
    
    /**
     * 根据标准ID删除（JPA方法，可能延迟）
     */
    
 void deleteByStandard(Standard standard);
    
    List<StandardPosition> findByStandard(Standard standard);
    
    // 查询某个职位是否已关联标准
 @Query("SELECT COUNT(sp) > 0 FROM StandardPosition sp WHERE sp.position.posId = :positionId")
    boolean existsByPositionId(@Param("positionId") Long positionId);

    // 查找标准关联的职位（包含职位信息）
    @Query("SELECT sp FROM StandardPosition sp " +
           "LEFT JOIN FETCH sp.position p " +
           "WHERE sp.standard = :standard " +
           "ORDER BY p.posName")
    List<StandardPosition> findByStandardWithPosition(@Param("standard") Standard standard);


     /**
     * 查询职位是否被其他标准关联（排除当前标准）
     */
    @Query("SELECT COUNT(sp) > 0 FROM StandardPosition sp WHERE sp.position.posId = :positionId AND sp.standard.standardId != :standardId")
    boolean existsByPositionIdAndStandardIdNot(@Param("positionId") Long positionId, @Param("standardId") Long standardId);
    
    /**
     * 查询职位关联的所有标准（排除当前标准）
     */
    @Query("SELECT sp FROM StandardPosition sp WHERE sp.position.posId = :positionId AND sp.standard.standardId != :standardId")
    List<StandardPosition> findByPositionIdAndStandardIdNot(@Param("positionId") Long positionId, @Param("standardId") Long standardId);

     // ========== 新增方法：查询已占用职位 ==========
    
    /**
     * 获取所有已被占用的职位ID列表
     * @return 已被占用的职位ID列表
     */
    @Query("SELECT DISTINCT sp.position.posId FROM StandardPosition sp")
    List<Long> findAllOccupiedPositionIds();
    
    /**
     * 获取指定标准关联的职位ID列表
     * @param standardId 标准ID
     * @return 该标准关联的职位ID列表
     */
    @Query("SELECT sp.position.posId FROM StandardPosition sp WHERE sp.standard.standardId = :standardId")
    List<Long> findPositionIdsByStandardId(@Param("standardId") Long standardId);
    
    /**
     * 批量检查多个职位是否被占用
     * @param positionIds 职位ID列表
     * @return 被占用的职位ID列表
     */
    @Query("SELECT DISTINCT sp.position.posId FROM StandardPosition sp WHERE sp.position.posId IN :positionIds")
    List<Long> findOccupiedPositionIdsByIds(@Param("positionIds") List<Long> positionIds);
    
    /**
     * 统计职位的被占用次数
     * @param positionId 职位ID
     * @return 被占用的次数
     */
    @Query("SELECT COUNT(sp) FROM StandardPosition sp WHERE sp.position.posId = :positionId")
    Long countOccupiedByPositionId(@Param("positionId") Long positionId);
    
    /**
     * 查找职位关联的所有标准（包含标准信息）
     * @param positionId 职位ID
     * @return 职位关联的标准列表
     */
    @Query("SELECT sp FROM StandardPosition sp " +
           "LEFT JOIN FETCH sp.standard s " +
           "WHERE sp.position.posId = :positionId " +
           "ORDER BY s.standardId")
    List<StandardPosition> findByPositionIdWithStandard(@Param("positionId") Long positionId);
    
    /**
     * 获取职位的占用详细信息（包含占用它的标准信息）
     * @param positionId 职位ID
     * @param excludeStandardId 要排除的标准ID（可为null）
     * @return 占用详细列表
     */
    @Query("SELECT new org.example.hrm.dto.OccupationDetailDTO(" +
           "s.standardId, s.standardCode, s.standardName, s.status, " +
           "s.creatorId, s.createdAt) " +
           "FROM StandardPosition sp " +
           "JOIN sp.standard s " +
           "WHERE sp.position.posId = :positionId " +
           "AND (:excludeStandardId IS NULL OR s.standardId != :excludeStandardId)")
    List<OccupationDetailDTO> findOccupationDetailsByPositionId(
            @Param("positionId") Long positionId,
            @Param("excludeStandardId") Long excludeStandardId);
    
    /**
     * 检查职位是否被指定标准关联
     * @param positionId 职位ID
     * @param standardId 标准ID
     * @return 是否被关联
     */
    @Query("SELECT COUNT(sp) > 0 FROM StandardPosition sp WHERE sp.position.posId = :positionId AND sp.standard.standardId = :standardId")
    boolean isPositionOccupiedByStandard(@Param("positionId") Long positionId, @Param("standardId") Long standardId);


    
    
}

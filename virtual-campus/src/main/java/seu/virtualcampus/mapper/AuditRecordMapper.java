package seu.virtualcampus.mapper;


import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.AuditRecord;

import java.util.List;

/**
 * 审核记录Mapper接口。
 * <p>
 * 定义了与数据库中audit_record表相关的操作。
 */
@Mapper
public interface AuditRecordMapper {
    /**
     * 插入一条新的审核记录。
     *
     * @param r 要插入的审核记录对象。
     */
    @Insert("INSERT INTO audit_record(student_id, field, old_value, new_value, status, create_time) VALUES(#{studentId}, #{field}, #{oldValue}, #{newValue}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AuditRecord r);

    /**
     * 查询所有状态为'pending'（待审核）的审核记录。
     *
     * @return 待审核记录的列表。
     */
    @Select("SELECT id, student_id AS studentId, field, old_value AS oldValue, new_value AS newValue, status, reviewer_id AS reviewerId, remark, create_time AS createTime, review_time AS reviewTime FROM audit_record WHERE status = 'pending'")
    List<AuditRecord> findPending();

    /**
     * 根据ID查询一条审核记录。
     *
     * @param id 审核记录的ID。
     * @return 对应的审核记录对象，如果不存在则返回null。
     */
    @Select("SELECT id, student_id AS studentId, field, old_value AS oldValue, new_value AS newValue, status, reviewer_id AS reviewerId FROM audit_record WHERE id = #{id}")
    AuditRecord findById(Long id);

    /**
     * 更新一条审核记录的状态。
     *
     * @param id         要更新的记录ID。
     * @param status     新的状态。
     * @param reviewerId 审核员ID。
     * @param remark     审核备注。
     * @param reviewTime 审核时间。
     * @return 受影响的行数。
     */
    @Update("UPDATE audit_record SET status = #{status}, reviewer_id = #{reviewerId}, remark = #{remark}, review_time = #{reviewTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("reviewerId") Long reviewerId, @Param("remark") String remark, @Param("reviewTime") String reviewTime);

    /**
     * 根据学生ID查询其所有的审核记录。
     *
     * @param studentId 学生ID。
     * @return 该学生的所有审核记录列表，按创建时间降序排列。
     */
    @Select("SELECT id, student_id AS studentId, field, old_value AS oldValue, new_value AS newValue, status, reviewer_id AS reviewerId, remark, create_time AS createTime, review_time AS reviewTime FROM audit_record WHERE student_id = #{studentId} ORDER BY create_time DESC")
    List<AuditRecord> findByStudentId(Long studentId);
}
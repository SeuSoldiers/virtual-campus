package seu.virtualcampus.mapper;


import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.AuditRecord;

import java.util.List;


@Mapper
public interface AuditRecordMapper {
    @Insert("INSERT INTO audit_record(student_id, field, old_value, new_value, status, create_time) VALUES(#{studentId}, #{field}, #{oldValue}, #{newValue}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AuditRecord r);


    @Select("SELECT id, student_id AS studentId, field, old_value AS oldValue, new_value AS newValue, status, reviewer_id AS reviewerId, remark, create_time AS createTime, review_time AS reviewTime FROM audit_record WHERE status = 'pending'")
    List<AuditRecord> findPending();


    @Select("SELECT id, student_id AS studentId, field, old_value AS oldValue, new_value AS newValue, status, reviewer_id AS reviewerId FROM audit_record WHERE id = #{id}")
    AuditRecord findById(Long id);

    @Update("UPDATE audit_record SET status = #{status}, reviewer_id = #{reviewerId}, remark = #{remark}, review_time = #{reviewTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("reviewerId") Long reviewerId, @Param("remark") String remark, @Param("reviewTime") String reviewTime);

    @Select("SELECT id, student_id AS studentId, field, old_value AS oldValue, new_value AS newValue, status, reviewer_id AS reviewerId, remark, create_time AS createTime, review_time AS reviewTime FROM audit_record WHERE student_id = #{studentId} ORDER BY create_time DESC")
    List<AuditRecord> findByStudentId(Long studentId);
}
package seu.virtualcampus.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核记录实体类。
 * <p>
 * 用于记录学生信息修改的审核过程。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {
    /**
     * 审核记录的唯一标识ID。
     */
    private Long id;
    /**
     * 提交审核的学生的ID。
     */
    private Long studentId;
    /**
     * 被修改的字段名。
     */
    private String field;
    /**
     * 字段的原始值。
     */
    private String oldValue;
    /**
     * 字段申请修改的新值。
     */
    private String newValue;
    /**
     * 审核状态。
     * <p>
     * 可能的值: "pending" (待审核), "approved" (已批准), "rejected" (已驳回)。
     */
    private String status; // pending, approved, rejected
    /**
     * 执行审核操作的管理员ID。
     */
    private Long reviewerId;
    /**
     * 审核意见或备注。
     */
    private String remark; // 审核意见
    /**
     * 审核记录的创建时间，格式如 yyyy-MM-dd HH:mm:ss。
     */
    private String createTime; // 创建时间，格式如 yyyy-MM-dd HH:mm:ss
    /**
     * 审核操作的执行时间，格式如 yyyy-MM-dd HH:mm:ss。
     */
    private String reviewTime; // 审核时间，格式如 yyyy-MM-dd HH:mm:ss
}
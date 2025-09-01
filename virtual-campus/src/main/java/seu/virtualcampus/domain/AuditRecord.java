package seu.virtualcampus.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {
    private Long id;
    private Long studentId;
    private String field;
    private String oldValue;
    private String newValue;
    private String status; // pending, approved, rejected
    private Long reviewerId;
    private String remark; // 审核意见
    private String createTime; // 创建时间，格式如 yyyy-MM-dd HH:mm:ss
    private String reviewTime; // 审核时间，格式如 yyyy-MM-dd HH:mm:ss
}
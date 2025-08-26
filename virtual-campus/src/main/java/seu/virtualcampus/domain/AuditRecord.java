package seu.virtualcampus.domain;


import lombok.Data;


@Data
public class AuditRecord {
    private Long id;
    private Long studentId;
    private String field;
    private String oldValue;
    private String newValue;
    private String status; // pending, approved, rejected
    private Long reviewerId;
}
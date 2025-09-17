package seu.virtualcampus.service;


import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.AuditRecord;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.mapper.AuditRecordMapper;
import seu.virtualcampus.mapper.StudentInfoMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审核服务类。
 * <p>
 * 提供与学生信息修改审核相关的业务逻辑。
 */
@Service
public class AuditService {
    private final AuditRecordMapper auditRecordMapper;
    private final StudentInfoMapper studentInfoMapper;

    /**
     * AuditService的构造函数。
     *
     * @param auditRecordMapper 审核记录Mapper，用于数据库操作。
     * @param studentInfoMapper 学生信息Mapper，用于在审核通过后更新学生信息。
     */
    public AuditService(AuditRecordMapper auditRecordMapper, StudentInfoMapper studentInfoMapper) {
        this.auditRecordMapper = auditRecordMapper;
        this.studentInfoMapper = studentInfoMapper;
    }

    /**
     * 获取所有待审核的记录列表。
     *
     * @return 待审核记录的列表。
     */
    public List<AuditRecord> listPending() {
        return auditRecordMapper.findPending();
    }

    /**
     * 执行审核操作。
     * <p>
     * 审核通过则更新审核记录状态并同步学生信息，审核驳回则仅更新状态。
     *
     * @param auditId    要审核的记录ID。
     * @param reviewerId 审核员ID。
     * @param approve    是否批准，true为批准，false为驳回。
     * @param remark     审核备注。
     * @return 操作成功返回true，记录不存在或状态不正确返回false。
     */
    public boolean review(Long auditId, Long reviewerId, boolean approve, String remark) {
        AuditRecord r = auditRecordMapper.findById(auditId);
        if (r == null || !"pending".equals(r.getStatus())) return false;
        String newStatus = approve ? "approved" : "rejected";
        String reviewTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int updated = auditRecordMapper.updateStatus(auditId, newStatus, reviewerId, remark, reviewTime);
        if (updated <= 0) return false;
        if (approve) {
            StudentInfo s = studentInfoMapper.findById(r.getStudentId());
            if (s == null) s = new StudentInfo();
            s.setStudentId(r.getStudentId());
            switch (r.getField()) {
                case "name":
                    s.setName(r.getNewValue());
                    break;
                case "major":
                    s.setMajor(r.getNewValue());
                    break;
                case "address":
                    s.setAddress(r.getNewValue());
                    break;
                case "phone":
                    s.setPhone(r.getNewValue());
                    break;
            }
            studentInfoMapper.update(s);
        }
        return true;
    }

    /**
     * 根据学生ID获取其所有的审核记录。
     *
     * @param studentId 学生ID。
     * @return 该学生的所有审核记录列表。
     */
    public List<AuditRecord> listByStudentId(Long studentId) {
        return auditRecordMapper.findByStudentId(studentId);
    }
}
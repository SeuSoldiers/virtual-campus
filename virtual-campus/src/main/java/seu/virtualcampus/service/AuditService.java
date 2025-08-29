package seu.virtualcampus.service;


import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.AuditRecord;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.mapper.AuditRecordMapper;
import seu.virtualcampus.mapper.StudentInfoMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class AuditService {
    private final AuditRecordMapper auditRecordMapper;
    private final StudentInfoMapper studentInfoMapper;


    public AuditService(AuditRecordMapper auditRecordMapper, StudentInfoMapper studentInfoMapper) {
        this.auditRecordMapper = auditRecordMapper;
        this.studentInfoMapper = studentInfoMapper;
    }


    public List<AuditRecord> listPending() {
        return auditRecordMapper.findPending();
    }


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


    public List<AuditRecord> listByStudentId(Long studentId) {
        return auditRecordMapper.findByStudentId(studentId);
    }
}
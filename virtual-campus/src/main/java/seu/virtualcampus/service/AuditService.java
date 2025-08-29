package seu.virtualcampus.service;


import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.AuditRecord;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.mapper.AuditRecordMapper;
import seu.virtualcampus.mapper.StudentInfoMapper;

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


    public boolean review(Long auditId, Long reviewerId, boolean approve) {
        AuditRecord r = auditRecordMapper.findById(auditId);
        if (r == null || !"pending".equals(r.getStatus())) return false;
        String newStatus = approve ? "approved" : "rejected";
        int updated = auditRecordMapper.updateStatus(auditId, newStatus, reviewerId);
        if (updated <= 0) return false;
        if (approve) {
// apply to student_info
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
// if record existed update else insert
            if (studentInfoMapper.findById(s.getStudentId()) == null) studentInfoMapper.insert(s);
            else studentInfoMapper.update(s);
        }
        return true;
    }


    public List<AuditRecord> listByStudentId(Long studentId) {
        return auditRecordMapper.findByStudentId(studentId);
    }
}
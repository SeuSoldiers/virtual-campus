package seu.virtualcampus.service;


import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.AuditRecord;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.mapper.AuditRecordMapper;
import seu.virtualcampus.mapper.StudentInfoMapper;


@Service
public class StudentInfoService {
    private final StudentInfoMapper studentInfoMapper;
    private final AuditRecordMapper auditRecordMapper;


    public StudentInfoService(StudentInfoMapper studentInfoMapper, AuditRecordMapper auditRecordMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.auditRecordMapper = auditRecordMapper;
    }


    public StudentInfo getStudentInfo(Long studentId) {
        return studentInfoMapper.findById(studentId);
    }


    /**
     * Submit changes as pending audit records. For simplicity we compare fields and create
     * per-field audit records for changed fields.
     */
    public void submitChanges(Long studentId, StudentInfo updated) {
        StudentInfo original = studentInfoMapper.findById(studentId);
        if (original == null) {
// if no original, insert directly and also create 'pending' records for full data
            studentInfoMapper.insert(updated);
// create audit records for each non-null field
            createAuditForField(studentId, "name", null, updated.getName());
            createAuditForField(studentId, "major", null, updated.getMajor());
            createAuditForField(studentId, "address", null, updated.getAddress());
            createAuditForField(studentId, "phone", null, updated.getPhone());
            return;
        }


        if (notEquals(original.getName(), updated.getName()))
            createAuditForField(studentId, "name", original.getName(), updated.getName());
        if (notEquals(original.getMajor(), updated.getMajor()))
            createAuditForField(studentId, "major", original.getMajor(), updated.getMajor());
        if (notEquals(original.getAddress(), updated.getAddress()))
            createAuditForField(studentId, "address", original.getAddress(), updated.getAddress());
        if (notEquals(original.getPhone(), updated.getPhone()))
            createAuditForField(studentId, "phone", original.getPhone(), updated.getPhone());
    }


    private boolean notEquals(String a, String b) {
        if (a == null && b == null) return false;
        if (a == null) return true;
        return !a.equals(b);
    }


    private void createAuditForField(Long studentId, String field, String oldValue, String newValue) {
        AuditRecord r = new AuditRecord();
        r.setStudentId(studentId);
        r.setField(field);
        r.setOldValue(oldValue);
        r.setNewValue(newValue);
        r.setStatus("pending");
        auditRecordMapper.insert(r);
    }
}
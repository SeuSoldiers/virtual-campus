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
     * Submit changes as pending audit records. For simplicity, we compare fields and create
     * per-field audit records for changed fields.
     */
    public void submitChanges(Long studentId, StudentInfo updated) {
        StudentInfo original = studentInfoMapper.findById(studentId);
        // 不论首次还是后续修改，都不直接写 student_info，只生成审核记录
        if (original == null) {
            if (notEmpty(updated.getName()))
                createAuditForField(studentId, "name", null, updated.getName());
            if (notEmpty(updated.getMajor()))
                createAuditForField(studentId, "major", null, updated.getMajor());
            if (notEmpty(updated.getAddress()))
                createAuditForField(studentId, "address", null, updated.getAddress());
            if (notEmpty(updated.getPhone()))
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


    private boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }


    private void createAuditForField(Long studentId, String field, String oldValue, String newValue) {
        AuditRecord r = new AuditRecord();
        r.setStudentId(studentId);
        r.setField(field);
        r.setOldValue(oldValue);
        r.setNewValue(newValue);
        r.setStatus("pending");
        r.setRemark("");
        // 使用标准时间格式 yyyy-MM-dd HH:mm:ss
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = java.time.LocalDateTime.now().format(formatter);
        r.setCreateTime(now);
        auditRecordMapper.insert(r);
    }
}
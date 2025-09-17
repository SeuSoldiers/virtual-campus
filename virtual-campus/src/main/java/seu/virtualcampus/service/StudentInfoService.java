package seu.virtualcampus.service;


import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.AuditRecord;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.mapper.AuditRecordMapper;
import seu.virtualcampus.mapper.StudentInfoMapper;


/**
 * 学生信息服务类。
 * <p>
 * 提供学生信息的查询与变更申请（需审核）等相关业务逻辑。
 */
@Service
public class StudentInfoService {
    private final StudentInfoMapper studentInfoMapper;
    private final AuditRecordMapper auditRecordMapper;


    public StudentInfoService(StudentInfoMapper studentInfoMapper, AuditRecordMapper auditRecordMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.auditRecordMapper = auditRecordMapper;
    }


    /**
     * 根据学生ID获取学生信息。
     *
     * @param studentId 学生ID。
     * @return 对应的学生信息对象，若不存在则返回null。
     */
    public StudentInfo getStudentInfo(Long studentId) {
        return studentInfoMapper.findById(studentId);
    }


    /**
     * 提交学生信息变更，生成待审核记录。
     * <p>
     * 会对比原有信息与新信息，仅对有变更的字段生成审核记录。
     *
     * @param studentId 学生ID。
     * @param updated   新的学生信息对象。
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
            if (notEmpty(updated.getEthnicity()))
                createAuditForField(studentId, "ethnicity", null, updated.getEthnicity());
            if (notEmpty(updated.getPoliticalStatus()))
                createAuditForField(studentId, "politicalStatus", null, updated.getPoliticalStatus());
            if (notEmpty(updated.getGender()))
                createAuditForField(studentId, "gender", null, updated.getGender());
            if (notEmpty(updated.getPlaceOfOrigin()))
                createAuditForField(studentId, "placeOfOrigin", null, updated.getPlaceOfOrigin());
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
        if (notEquals(original.getEthnicity(), updated.getEthnicity()))
            createAuditForField(studentId, "ethnicity", original.getEthnicity(), updated.getEthnicity());
        if (notEquals(original.getPoliticalStatus(), updated.getPoliticalStatus()))
            createAuditForField(studentId, "politicalStatus", original.getPoliticalStatus(), updated.getPoliticalStatus());
        if (notEquals(original.getGender(), updated.getGender()))
            createAuditForField(studentId, "gender", original.getGender(), updated.getGender());
        if (notEquals(original.getPlaceOfOrigin(), updated.getPlaceOfOrigin()))
            createAuditForField(studentId, "placeOfOrigin", original.getPlaceOfOrigin(), updated.getPlaceOfOrigin());
    }


    /**
     * 判断两个字符串是否不相等。
     *
     * @param a 字符串a。
     * @param b 字符串b。
     * @return 不相等返回true，相等返回false。
     */
    private boolean notEquals(String a, String b) {
        if (a == null && b == null) return false;
        if (a == null) return true;
        return !a.equals(b);
    }


    /**
     * 判断字符串是否非空。
     *
     * @param s 待判断字符串。
     * @return 非空返回true，否则返回false。
     */
    private boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }


    /**
     * 为指定字段创建审核记录。
     *
     * @param studentId 学生ID。
     * @param field     字段名。
     * @param oldValue  原值。
     * @param newValue  新值。
     */
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
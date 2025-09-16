package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.BorrowRecord;
import seu.virtualcampus.mapper.BorrowRecordMapper;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowRecordService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    /** 新增借阅记录 */
    public void addBorrowRecord(BorrowRecord record) {
        borrowRecordMapper.insert(record);
    }

    /** 更新借阅记录（一般不直接用） */
    public void updateBorrowRecord(BorrowRecord record) {
        borrowRecordMapper.update(record);
    }

    /** 删除记录 */
    public void deleteBorrowRecord(String recordId) {
        borrowRecordMapper.delete(recordId);
    }

    /** 查找 */
    public BorrowRecord getById(String recordId) {
        return borrowRecordMapper.findById(recordId);
    }

    public List<BorrowRecord> getByUser(String userId) {
        return borrowRecordMapper.findByUserId(userId);
    }

    public List<BorrowRecord> getByBook(String bookId) {
        return borrowRecordMapper.findByBookId(bookId);
    }

    public List<BorrowRecord> getActiveByUser(String userId) {
        return borrowRecordMapper.findActiveByUserId(userId);
    }

    public List<BorrowRecord> getActiveByBook(String bookId) {
        return borrowRecordMapper.findActiveByBookId(bookId);
    }

    /** 归还 */
    @Transactional
    public boolean returnBook(String recordId, String returnDate) {
        return borrowRecordMapper.returnBook(recordId, returnDate) > 0;
    }

    /** 续借 */
    @Transactional
    public boolean renewBook(String recordId, String newDueDate) {
        return borrowRecordMapper.renewBook(recordId, newDueDate) > 0;
    }

    /** 用户是否超过借阅上限 */
    public boolean canBorrow(String userId, int maxAllowed) {
        int active = borrowRecordMapper.countActiveByUser(userId);
        return active < maxAllowed;
    }

    /** 检查并标记逾期 */
    @Transactional
    public int markOverdueIfNeeded() {
        String today = LocalDate.now().toString();
        return borrowRecordMapper.markOverdueByDate(today);
    }

    public List<BorrowRecord> getAll() {
        return borrowRecordMapper.findAll();
    }

    public String generateRecordId() {
        int count = getAll().size();   // 获取所有借阅记录数量
        return "R" + String.format("%03d", count + 1); // 生成形如 R001, R002
    }
}

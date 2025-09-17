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

    /** 借书（30天，renewCount=0） */
    @Transactional
    public void borrowBook(String userId, String bookId) {
        // 1. 检查借阅上限（假设上限 5 本，可调整）
        if (!canBorrow(userId, 5)) {
            throw new RuntimeException("超过最大借阅数量（5本）");
        }

        // 2. 检查该副本是否已经被借出
        BorrowRecord active = borrowRecordMapper.findLatestActiveByBookId(bookId);
        if (active != null) {
            throw new RuntimeException("该书副本已被借出");
        }

        // 3. 生成借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(generateRecordId());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(30));
        record.setReturnDate(null);
        record.setRenewCount(0); //
        record.setStatus("BORROWED");

        borrowRecordMapper.insert(record);
    }

    /** 续借（最多2次，每次+30天） */
    @Transactional
    public void renewBorrow(String recordId) {
        BorrowRecord record = borrowRecordMapper.findById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        if (record.getRenewCount() != null && record.getRenewCount() >= 2) {
            throw new RuntimeException("已达到最大续借次数（2次）");
        }

        LocalDate newDueDate = record.getDueDate().plusDays(30);
        borrowRecordMapper.renewBook(recordId, newDueDate.toString());
    }

    /** 归还 */
    @Transactional
    public boolean returnBook(String recordId, String returnDate) {
        return borrowRecordMapper.returnBook(recordId, returnDate) > 0;
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

    /** 续借 */
    @Transactional
    public boolean renewBook(String recordId, String newDueDate) {
        return borrowRecordMapper.renewBook(recordId, newDueDate) > 0;
    }

    public List<BorrowRecord> getAll() {
        return borrowRecordMapper.findAll();
    }

    public String generateRecordId() {
        int count = getAll().size();   // 获取所有借阅记录数量
        return "R" + String.format("%03d", count + 1); // 生成形如 R001, R002
    }
}

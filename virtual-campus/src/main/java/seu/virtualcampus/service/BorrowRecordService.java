package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.BorrowRecord;
import seu.virtualcampus.mapper.BorrowRecordMapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 借阅记录服务类。
 * <p>
 * 提供借阅记录的增删改查、归还、续借、逾期标记等相关业务逻辑。
 * </p>
 */
@Service
public class BorrowRecordService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    /**
     * 新增借阅记录。
     *
     * @param record 借阅记录对象。
     */
    public void addBorrowRecord(BorrowRecord record) {
        borrowRecordMapper.insert(record);
    }

    /**
     * 更新借阅记录（一般不直接用）。
     *
     * @param record 借阅记录对象。
     */
    public void updateBorrowRecord(BorrowRecord record) {
        borrowRecordMapper.update(record);
    }

    /**
     * 删除借阅记录。
     *
     * @param recordId 借阅记录ID。
     */
    public void deleteBorrowRecord(String recordId) {
        borrowRecordMapper.delete(recordId);
    }

    /**
     * 根据ID查找借阅记录。
     *
     * @param recordId 借阅记录ID。
     * @return 对应的借阅记录对象，若不存在则返回null。
     */
    public BorrowRecord getById(String recordId) {
        return borrowRecordMapper.findById(recordId);
    }

    /**
     * 根据用户ID查找借阅记录。
     *
     * @param userId 用户ID。
     * @return 该用户的所有借阅记录列表。
     */
    public List<BorrowRecord> getByUser(String userId) {
        return borrowRecordMapper.findByUserId(userId);
    }

    /**
     * 根据图书副本ID查找借阅记录。
     *
     * @param bookId 图书副本ID。
     * @return 该副本的所有借阅记录列表。
     */
    public List<BorrowRecord> getByBook(String bookId) {
        return borrowRecordMapper.findByBookId(bookId);
    }

    /**
     * 获取用户当前所有未归还的借阅记录。
     *
     * @param userId 用户ID。
     * @return 该用户的所有未归还借阅记录列表。
     */
    public List<BorrowRecord> getActiveByUser(String userId) {
        return borrowRecordMapper.findActiveByUserId(userId);
    }

    /**
     * 获取副本当前所有未归还的借阅记录。
     *
     * @param bookId 图书副本ID。
     * @return 该副本的所有未归还借阅记录列表。
     */
    public List<BorrowRecord> getActiveByBook(String bookId) {
        return borrowRecordMapper.findActiveByBookId(bookId);
    }

    /**
     * 归还图书。
     *
     * @param recordId   借阅记录ID。
     * @param returnDate 归还日期。
     * @return 归还成功返回true，否则返回false。
     */
    @Transactional
    public boolean returnBook(String recordId, String returnDate) {
        return borrowRecordMapper.returnBook(recordId, returnDate) > 0;
    }

    /**
     * 续借图书。
     *
     * @param recordId   借阅记录ID。
     * @param newDueDate 新的到期日期。
     * @return 续借成功返回true，否则返回false。
     */
    @Transactional
    public boolean renewBook(String recordId, String newDueDate) {
        return borrowRecordMapper.renewBook(recordId, newDueDate) > 0;
    }

    /**
     * 判断用户是否超出借阅上限。
     *
     * @param userId     用户ID。
     * @param maxAllowed 最大允许借阅数。
     * @return 未超出返回true，超出返回false。
     */
    public boolean canBorrow(String userId, int maxAllowed) {
        int active = borrowRecordMapper.countActiveByUser(userId);
        return active < maxAllowed;
    }

    /**
     * 检查并标记逾期。
     *
     * @return 本次被标记为逾期的记录数。
     */
    @Transactional
    public int markOverdueIfNeeded() {
        String today = LocalDate.now().toString();
        return borrowRecordMapper.markOverdueByDate(today);
    }

    /**
     * 获取所有借阅记录。
     *
     * @return 所有借阅记录列表。
     */
    public List<BorrowRecord> getAll() {
        return borrowRecordMapper.findAll();
    }

    /**
     * 生成新的借阅记录ID。
     *
     * @return 新的借阅记录ID（如R001、R002等）。
     */
    public String generateRecordId() {
        int count = getAll().size();   // 获取所有借阅记录数量
        return "R" + String.format("%03d", count + 1); // 生成形如 R001, R002
    }
}

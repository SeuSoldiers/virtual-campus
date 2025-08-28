package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.BorrowRecord;
import seu.virtualcampus.mapper.BorrowRecordMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BorrowRecordService {
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookService bookService;

    @Transactional
    public BorrowRecord borrowBook(String userId, String bookId) {
        // 先尝试减少可用数量
        boolean success = bookService.borrowBook(bookId);
        if (!success) {
            return null; // 图书不可借
        }

        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(30)); // 默认借阅30天
        record.setRenewCount(0);
        record.setStatus("BORROWED");

        borrowRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public void returnBook(String recordId) {
        BorrowRecord record = borrowRecordMapper.findById(recordId);
        if (record != null) {
            // 更新归还日期和状态
            borrowRecordMapper.returnBook(recordId, LocalDate.now().toString());
            // 增加图书可用数量
            bookService.returnBook(record.getBookId());
        }
    }

    @Transactional
    public boolean renewBook(String recordId) {
        BorrowRecord record = borrowRecordMapper.findById(recordId);
        if (record != null && record.getRenewCount() < 3) { // 假设最多续借3次
            LocalDate newDueDate = record.getDueDate().plusDays(30);
            borrowRecordMapper.renewBook(recordId, newDueDate.toString());
            return true;
        }
        return false;
    }

    public List<BorrowRecord> getBorrowRecordsByUser(String userId) {
        return borrowRecordMapper.findByUserId(userId);
    }

    public List<BorrowRecord> getActiveBorrowRecordsByUser(String userId) {
        return borrowRecordMapper.findActiveByUserId(userId);
    }

    public List<BorrowRecord> getBorrowRecordsByBook(String bookId) {
        return borrowRecordMapper.findByBookId(bookId);
    }

    public BorrowRecord getBorrowRecordById(String recordId) {
        return borrowRecordMapper.findById(recordId);
    }

    public List<BorrowRecord> getBorrowRecordsByUserAndBook(String userId, String bookId) {
        return borrowRecordMapper.findByUserAndBook(userId, bookId);
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordMapper.findAll();
    }


}
package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.ReservationRecord;
import seu.virtualcampus.mapper.ReservationRecordMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationRecordService {
    @Autowired
    private ReservationRecordMapper reservationRecordMapper;

    @Autowired
    private BookService bookService;

    @Transactional
    public ReservationRecord reserveBook(String userId, String bookId) {
        // 获取当前预约排队位置
        int queuePosition = reservationRecordMapper.countActiveByBookId(bookId) + 1;

        // 创建预约记录
        ReservationRecord record = new ReservationRecord();
        record.setReservationId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setReserveDate(LocalDate.now());
        record.setStatus("ACTIVE");
        record.setQueuePosition(queuePosition);
        record.setNotifyStatus("NOT_NOTIFIED");

        reservationRecordMapper.insert(record);

        // 增加图书预约计数
        bookService.increaseReservationCount(bookId);

        return record;
    }

    @Transactional
    public void cancelReservation(String reservationId) {
        ReservationRecord record = reservationRecordMapper.findById(reservationId);
        if (record != null) {
            reservationRecordMapper.cancelReservation(reservationId);
            // 减少图书预约计数
            bookService.decreaseReservationCount(record.getBookId());
            // 更新排队位置
            reservationRecordMapper.decreaseQueuePositions(record.getBookId(), record.getQueuePosition());
        }
    }

    @Transactional
    public void processReservationOnReturn(String bookId) {
        // 获取最早的有效预约
        List<ReservationRecord> activeReservations = reservationRecordMapper.findActiveByBookId(bookId);
        if (!activeReservations.isEmpty()) {
            ReservationRecord firstReservation = activeReservations.get(0);
            // 标记为已通知
            reservationRecordMapper.markAsNotified(firstReservation.getReservationId());
            // 这里可以添加发送通知的逻辑
        }
    }

    public List<ReservationRecord> getUserReservations(String userId) {
        return reservationRecordMapper.findByUserId(userId);
    }

    public List<ReservationRecord> getActiveReservationsByBook(String bookId) {
        return reservationRecordMapper.findActiveByBookId(bookId);
    }

    public List<ReservationRecord> getReservationsByUserAndBook(String userId, String bookId) {
        return reservationRecordMapper.findByUserAndBook(userId, bookId);
    }

    public List<ReservationRecord> getAllReservations() {
        return reservationRecordMapper.findAll();
    }

    public List<ReservationRecord> getReservationsByStatus(String status) {
        return reservationRecordMapper.findByStatus(status);
    }

    public List<ReservationRecord> getReservationsByNotifyStatus(String notifyStatus) {
        return reservationRecordMapper.findByNotifyStatus(notifyStatus);
    }

    public void updateReservationNotifyStatus(String reservationId, String notifyStatus) {
        ReservationRecord record = reservationRecordMapper.findById(reservationId);
        if (record != null) {
            record.setNotifyStatus(notifyStatus);
            reservationRecordMapper.update(record);
        }
    }
}
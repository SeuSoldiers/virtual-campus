package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.ReservationRecord;
import seu.virtualcampus.mapper.ReservationRecordMapper;

import java.util.List;

@Service
public class ReservationRecordService {

    @Autowired
    private ReservationRecordMapper reservationRecordMapper;

    /** 新增预约 */
    @Transactional
    public boolean addReservation(ReservationRecord record) {
        // 防止重复预约
        if (reservationRecordMapper.existsActiveByUserAndIsbn(record.getUserId(), record.getIsbn()) > 0) {
            return false;
        }
        // 计算队列位置
        Integer pos = reservationRecordMapper.nextQueuePosition(record.getIsbn());
        record.setQueuePosition(pos);
        reservationRecordMapper.insert(record);
        return true;
    }

    /** 更新预约（管理员或内部使用） */
    public void updateReservation(ReservationRecord record) {
        reservationRecordMapper.update(record);
    }

    /** 删除预约 */
    public void deleteReservation(String reservationId) {
        reservationRecordMapper.delete(reservationId);
    }

    public ReservationRecord getById(String reservationId) {
        return reservationRecordMapper.findById(reservationId);
    }

    public List<ReservationRecord> getByUser(String userId) {
        return reservationRecordMapper.findByUserId(userId);
    }

    public List<ReservationRecord> getByIsbn(String isbn) {
        return reservationRecordMapper.findByIsbn(isbn);
    }

    public List<ReservationRecord> getActiveByIsbn(String isbn) {
        return reservationRecordMapper.findActiveByIsbn(isbn);
    }

    /** 队首预约（用于归还后分配） */
    public ReservationRecord getFirstActiveByIsbn(String isbn) {
        return reservationRecordMapper.findFirstActiveByIsbn(isbn);
    }

    public int countActiveByIsbn(String isbn) {
        return reservationRecordMapper.countActiveByIsbn(isbn);
    }

    /** 取消预约 + 队列调整 */
    @Transactional
    public boolean cancelReservation(String reservationId) {
        ReservationRecord record = reservationRecordMapper.findById(reservationId);
        if (record == null ||
                !( "ACTIVE".equalsIgnoreCase(record.getStatus()) || "NOTIFIED".equalsIgnoreCase(record.getStatus()) )) {
            return false;
        }
        int pos = record.getQueuePosition();
        int updated = reservationRecordMapper.cancelReservation(reservationId);
        if (updated > 0) {
            reservationRecordMapper.decreaseQueuePositions(record.getIsbn(), pos);
            return true;
        }
        return false;
    }

    /** 预约兑现（用户拿到书）+ 队列调整 */
    @Transactional
    public boolean fulfillReservation(String reservationId) {
        ReservationRecord record = reservationRecordMapper.findById(reservationId);
        if (record == null) return false;

        if (!"ACTIVE".equalsIgnoreCase(record.getStatus())) {
            return false;
        }

        // 更新为 FULFILLED
        return reservationRecordMapper.updateStatus(
                reservationId, "FULFILLED") > 0;
    }

    public List<ReservationRecord> getAll() {
        return reservationRecordMapper.findAll();
    }

    public List<ReservationRecord> getByStatus(String status) {
        return reservationRecordMapper.findByStatus(status);
    }

    public String generateReservationId() {
        int count = getAll().size();   // 获取已有预约记录数
        return "RES" + String.format("%03d", count + 1); // 生成 RES001, RES002...
    }
}

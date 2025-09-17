package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.ReservationRecord;
import seu.virtualcampus.mapper.ReservationRecordMapper;

import java.util.List;

/**
 * 预约记录服务类。
 * <p>
 * 提供预约记录的增删改查、队列管理、预约兑现等相关业务逻辑。
 * </p>
 */
@Service
public class ReservationRecordService {

    @Autowired
    private ReservationRecordMapper reservationRecordMapper;

    /**
     * 新增预约。
     *
     * @param record 预约记录对象。
     * @return 新增成功返回true，重复预约返回false。
     */
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

    /**
     * 更新预约（管理员或内部使用）。
     *
     * @param record 预约记录对象。
     */
    public void updateReservation(ReservationRecord record) {
        reservationRecordMapper.update(record);
    }

    /**
     * 删除预约。
     *
     * @param reservationId 预约ID。
     */
    public void deleteReservation(String reservationId) {
        reservationRecordMapper.delete(reservationId);
    }

    /**
     * 根据ID获取预约记录。
     *
     * @param reservationId 预约ID。
     * @return 对应的预约记录对象，若不存在则返回null。
     */
    public ReservationRecord getById(String reservationId) {
        return reservationRecordMapper.findById(reservationId);
    }

    /**
     * 根据用户ID获取预约记录。
     *
     * @param userId 用户ID。
     * @return 该用户的所有预约记录列表。
     */
    public List<ReservationRecord> getByUser(String userId) {
        return reservationRecordMapper.findByUserId(userId);
    }

    /**
     * 根据ISBN获取预约记录。
     *
     * @param isbn 图书ISBN。
     * @return 该ISBN下的所有预约记录列表。
     */
    public List<ReservationRecord> getByIsbn(String isbn) {
        return reservationRecordMapper.findByIsbn(isbn);
    }

    /**
     * 获取指定ISBN的所有有效预约。
     *
     * @param isbn 图书ISBN。
     * @return 有效预约记录列表。
     */
    public List<ReservationRecord> getActiveByIsbn(String isbn) {
        return reservationRecordMapper.findActiveByIsbn(isbn);
    }

    /**
     * 获取指定ISBN的队首预约（用于归还后分配）。
     *
     * @param isbn 图书ISBN。
     * @return 队首的有效预约记录，若不存在则返回null。
     */
    public ReservationRecord getFirstActiveByIsbn(String isbn) {
        return reservationRecordMapper.findFirstActiveByIsbn(isbn);
    }

    /**
     * 统计指定ISBN的有效预约数量。
     *
     * @param isbn 图书ISBN。
     * @return 有效预约数量。
     */
    public int countActiveByIsbn(String isbn) {
        return reservationRecordMapper.countActiveByIsbn(isbn);
    }

    /**
     * 取消预约并调整队列。
     *
     * @param reservationId 预约ID。
     * @return 取消成功返回true，否则返回false。
     */
    @Transactional
    public boolean cancelReservation(String reservationId) {
        ReservationRecord record = reservationRecordMapper.findById(reservationId);
        if (record == null ||
                !("ACTIVE".equalsIgnoreCase(record.getStatus()) || "NOTIFIED".equalsIgnoreCase(record.getStatus()))) {
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

    /**
     * 预约兑现（用户取书）并调整队列。
     *
     * @param reservationId 预约ID。
     * @return 兑现成功返回true，否则返回false。
     */
    @Transactional
    public boolean fulfillReservation(String reservationId) {
        ReservationRecord record = reservationRecordMapper.findById(reservationId);
        if (record == null) return false;

        if (!"ACTIVE".equalsIgnoreCase(record.getStatus())) {
            return false;
        }

        int pos = record.getQueuePosition();
        int updated = reservationRecordMapper.updateStatus(reservationId, "FULFILLED");

        if (updated > 0) {
            reservationRecordMapper.decreaseQueuePositions(record.getIsbn(), pos);
            return true;
        }
        return false;
    }

    /**
     * 获取所有预约记录。
     *
     * @return 所有预约记录列表。
     */
    public List<ReservationRecord> getAll() {
        return reservationRecordMapper.findAll();
    }

    /**
     * 根据状态获取预约记录。
     *
     * @param status 预约状态。
     * @return 对应状态的预约记录列表。
     */
    public List<ReservationRecord> getByStatus(String status) {
        return reservationRecordMapper.findByStatus(status);
    }

    /**
     * 生成预约ID。
     *
     * @return 新的预约ID。
     */
    public String generateReservationId() {
        int count = getAll().size();   // 获取已有预约记录数
        return "RES" + String.format("%03d", count + 1); // 生成 RES001, RES002...
    }
}

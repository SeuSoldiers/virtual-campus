package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.ReservationRecord;
import java.util.List;

@Mapper
public interface ReservationRecordMapper {
    @Insert("INSERT INTO reservation_records(reservationId, userId, bookId, reserveDate, status, queuePosition, notifyStatus) " +
            "VALUES(#{reservationId}, #{userId}, #{bookId}, #{reserveDate}, #{status}, #{queuePosition}, #{notifyStatus})")
    void insert(ReservationRecord record);

    @Update("UPDATE reservation_records SET userId=#{userId}, bookId=#{bookId}, reserveDate=#{reserveDate}, " +
            "status=#{status}, queuePosition=#{queuePosition}, notifyStatus=#{notifyStatus} WHERE reservationId=#{reservationId}")
    void update(ReservationRecord record);

    @Select("SELECT * FROM reservation_records WHERE reservationId=#{reservationId}")
    ReservationRecord findById(String reservationId);

    @Select("SELECT * FROM reservation_records WHERE userId=#{userId}")
    List<ReservationRecord> findByUserId(String userId);

    @Select("SELECT * FROM reservation_records WHERE bookId=#{bookId}")
    List<ReservationRecord> findByBookId(String bookId);

    @Select("SELECT * FROM reservation_records WHERE bookId=#{bookId} AND status='ACTIVE' ORDER BY reserveDate ASC")
    List<ReservationRecord> findActiveByBookId(String bookId);

    @Select("SELECT COUNT(*) FROM reservation_records WHERE bookId=#{bookId} AND status='ACTIVE'")
    int countActiveByBookId(String bookId);

    @Update("UPDATE reservation_records SET status='CANCELLED' WHERE reservationId=#{reservationId}")
    void cancelReservation(String reservationId);

    @Update("UPDATE reservation_records SET status='FULFILLED' WHERE reservationId=#{reservationId}")
    void fulfillReservation(String reservationId);

    @Update("UPDATE reservation_records SET notifyStatus='NOTIFIED' WHERE reservationId=#{reservationId}")
    void markAsNotified(String reservationId);

    @Update("UPDATE reservation_records SET queuePosition = queuePosition - 1 WHERE bookId=#{bookId} AND status='ACTIVE' AND queuePosition > #{position}")
    void decreaseQueuePositions(@Param("bookId") String bookId, @Param("position") int position);

    @Select("SELECT * FROM reservation_records WHERE userId = #{userId} AND bookId = #{bookId}")
    List<ReservationRecord> findByUserAndBook(@Param("userId") String userId, @Param("bookId") String bookId);

    @Select("SELECT * FROM reservation_records")
    List<ReservationRecord> findAll();

    @Select("SELECT * FROM reservation_records WHERE status = #{status}")
    List<ReservationRecord> findByStatus(String status);

    @Select("SELECT * FROM reservation_records WHERE notifyStatus = #{notifyStatus}")
    List<ReservationRecord> findByNotifyStatus(String notifyStatus);
}
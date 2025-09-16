package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.ReservationRecord;

import java.util.List;

@Mapper
public interface ReservationRecordMapper {

    @Insert("""
        INSERT INTO reservation_records
        (reservationId, userId, isbn, reserveDate, status, queuePosition)
        VALUES
        (#{reservationId}, #{userId}, #{isbn}, #{reserveDate}, #{status}, #{queuePosition})
        """)
    int insert(ReservationRecord record);

    @Update("""
        UPDATE reservation_records
        SET userId=#{userId}, isbn=#{isbn}, reserveDate=#{reserveDate},
            status=#{status}, queuePosition=#{queuePosition}
        WHERE reservationId=#{reservationId}
        """)
    int update(ReservationRecord record);

    @Delete("DELETE FROM reservation_records WHERE reservationId=#{reservationId}")
    int delete(String reservationId);

    @Select("SELECT * FROM reservation_records WHERE reservationId=#{reservationId}")
    ReservationRecord findById(String reservationId);

    @Select("SELECT * FROM reservation_records WHERE userId=#{userId}")
    List<ReservationRecord> findByUserId(String userId);

    @Select("SELECT * FROM reservation_records WHERE isbn=#{isbn}")
    List<ReservationRecord> findByIsbn(String isbn);

    /** 某 ISBN 的有效排队（ACTIVE），按队列位次优先，其次按预约时间 */
    @Select("""
        SELECT * FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        ORDER BY queuePosition ASC, reserveDate ASC
        """)
    List<ReservationRecord> findActiveByIsbn(String isbn);

    /** 队列长度（ACTIVE 数量） */
    @Select("""
        SELECT COUNT(*)
        FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        """)
    int countActiveByIsbn(String isbn);

    /** 取队首（用于到书通知/自动分配），注意配合事务在 Service 层更新状态 */
    @Select("""
        SELECT * FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        ORDER BY queuePosition ASC, reserveDate ASC
        LIMIT 1
        """)
    ReservationRecord findFirstActiveByIsbn(String isbn);

    /** 计算下一位队列号（插入前调用） */
    @Select("""
        SELECT COALESCE(MAX(queuePosition), 0) + 1
        FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        """)
    Integer nextQueuePosition(String isbn);

    /** 取消预约（单条） */
    @Update("""
    UPDATE reservation_records
    SET status='CANCELLED'
    WHERE reservationId=#{reservationId} AND status='ACTIVE'
    """)
    int cancelReservation(String reservationId);


    /** 预约兑现（借到书） */
    @Update("UPDATE reservation_records SET status='FULFILLED' WHERE reservationId=#{reservationId} AND status='ACTIVE'")
    int fulfillReservation(String reservationId);

    /**
     * 队列位次回填：当某一位取消/兑现后，所有该位之后（> position）的 ACTIVE 预约，位次 -1
     * 需与取消/兑现操作放在同一事务中
     */
    @Update("""
        UPDATE reservation_records
        SET queuePosition = queuePosition - 1
        WHERE isbn=#{isbn} AND status='ACTIVE' AND queuePosition > #{position}
        """)
    int decreaseQueuePositions(@Param("isbn") String isbn, @Param("position") int position);

    /** 判断用户对某 ISBN 是否已有 ACTIVE 预约（防重复预约） */
    @Select("""
        SELECT COUNT(*)
        FROM reservation_records
        WHERE userId = #{userId} AND isbn = #{isbn} AND status='ACTIVE'
        """)
    int existsActiveByUserAndIsbn(@Param("userId") String userId, @Param("isbn") String isbn);

    @Select("""
        SELECT * FROM reservation_records
        WHERE userId = #{userId} AND isbn = #{isbn}
        ORDER BY reserveDate DESC
        """)
    List<ReservationRecord> findByUserAndIsbn(@Param("userId") String userId, @Param("isbn") String isbn);

    @Select("SELECT * FROM reservation_records")
    List<ReservationRecord> findAll();

    @Select("SELECT * FROM reservation_records WHERE status = #{status}")
    List<ReservationRecord> findByStatus(String status);

    @Update("""
    UPDATE reservation_records
    SET status = #{status}
    WHERE reservationId = #{reservationId}
    """)
    int updateStatus(@Param("reservationId") String reservationId,
                     @Param("status") String status);
}

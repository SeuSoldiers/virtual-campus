package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.ReservationRecord;

import java.util.List;

/**
 * 图书预约记录Mapper接口。
 * <p>
 * 定义了与数据库中reservation_records表相关的操作。
 */
@Mapper
public interface ReservationRecordMapper {

    /**
     * 插入一条新的预约记录。
     *
     * @param record 要插入的预约记录对象。
     * @return 受影响的行数。
     */
    @Insert("""
        INSERT INTO reservation_records
        (reservationId, userId, isbn, reserveDate, status, queuePosition)
        VALUES
        (#{reservationId}, #{userId}, #{isbn}, #{reserveDate}, #{status}, #{queuePosition})
        """)
    int insert(ReservationRecord record);

    /**
     * 更新一条预约记录的完整信息。
     *
     * @param record 包含更新信息的预约记录对象。
     * @return 受影响的行数。
     */
    @Update("""
        UPDATE reservation_records
        SET userId=#{userId}, isbn=#{isbn}, reserveDate=#{reserveDate},
            status=#{status}, queuePosition=#{queuePosition}
        WHERE reservationId=#{reservationId}
        """)
    int update(ReservationRecord record);

    /**
     * 根据ID删除一条预约记录。
     *
     * @param reservationId 要删除的预约记录ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM reservation_records WHERE reservationId=#{reservationId}")
    int delete(String reservationId);

    /**
     * 根据ID查询预约记录。
     *
     * @param reservationId 预约记录ID。
     * @return 对应的预约记录对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM reservation_records WHERE reservationId=#{reservationId}")
    ReservationRecord findById(String reservationId);

    /**
     * 根据用户ID查询其所有的预约记录。
     *
     * @param userId 用户ID。
     * @return 该用户的所有预约记录列表。
     */
    @Select("SELECT * FROM reservation_records WHERE userId=#{userId}")
    List<ReservationRecord> findByUserId(String userId);

    /**
     * 根据ISBN查询所有相关的预约记录。
     *
     * @param isbn 图书的ISBN。
     * @return 该ISBN对应的所有预约记录列表。
     */
    @Select("SELECT * FROM reservation_records WHERE isbn=#{isbn}")
    List<ReservationRecord> findByIsbn(String isbn);

    /**
     * 查询某ISBN的有效预约队列，按队列位置和预约时间排序。
     *
     * @param isbn 图书的ISBN。
     * @return 状态为'ACTIVE'的预约记录列表。
     */
    @Select("""
        SELECT * FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        ORDER BY queuePosition ASC, reserveDate ASC
        """)
    List<ReservationRecord> findActiveByIsbn(String isbn);

    /**
     * 统计某ISBN的有效预约队列长度。
     *
     * @param isbn 图书的ISBN。
     * @return 状态为'ACTIVE'的预约记录数量。
     */
    @Select("""
        SELECT COUNT(*)
        FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        """)
    int countActiveByIsbn(String isbn);

    /**
     * 获取某ISBN预约队列的队首记录。
     * <p>
     * 用于到书通知或自动分配，需在Service层配合事务更新状态。
     *
     * @param isbn 图书的ISBN。
     * @return 队列头部的预约记录，如果队列为空则返回null。
     */
    @Select("""
        SELECT * FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        ORDER BY queuePosition ASC, reserveDate ASC
        LIMIT 1
        """)
    ReservationRecord findFirstActiveByIsbn(String isbn);

    /**
     * 计算下一个可用的队列位置。
     * <p>
     * 在插入新预约记录前调用，以确定其在队列中的位置。
     *
     * @param isbn 图书的ISBN。
     * @return 下一个队列位置编号。
     */
    @Select("""
        SELECT COALESCE(MAX(queuePosition), 0) + 1
        FROM reservation_records
        WHERE isbn=#{isbn} AND status='ACTIVE'
        """)
    Integer nextQueuePosition(String isbn);

    /**
     * 取消一条预约记录。
     * <p>
     * 将记录状态从'ACTIVE'更新为'CANCELLED'。
     *
     * @param reservationId 要取消的预约记录ID。
     * @return 受影响的行数（1表示成功，0表示失败）。
     */
    @Update("""
    UPDATE reservation_records
    SET status='CANCELLED'
    WHERE reservationId=#{reservationId} AND status='ACTIVE'
    """)
    int cancelReservation(String reservationId);


    /**
     * 兑现一条预约记录（用户成功借到书）。
     * <p>
     * 将记录状态从'ACTIVE'更新为'FULFILLED'。
     *
     * @param reservationId 要兑现的预约记录ID。
     * @return 受影响的行数（1表示成功，0表示失败）。
     */
    @Update("UPDATE reservation_records SET status='FULFILLED' WHERE reservationId=#{reservationId} AND status='ACTIVE'")
    int fulfillReservation(String reservationId);

    /**
     * 递减队列位置。
     * <p>
     * 当队列中某位用户取消或兑现预约后，所有排在他后面的用户的队列位置减1。
     * 此操作需要与取消/兑现操作放在同一个事务中。
     *
     * @param isbn     图书的ISBN。
     * @param position 已离开队列的用户原来的位置。
     * @return 受影响的行数。
     */
    @Update("""
        UPDATE reservation_records
        SET queuePosition = queuePosition - 1
        WHERE isbn=#{isbn} AND status='ACTIVE' AND queuePosition > #{position}
        """)
    int decreaseQueuePositions(@Param("isbn") String isbn, @Param("position") int position);

    /**
     * 检查用户是否对某ISBN存在有效的预约，用于防止重复预约。
     *
     * @param userId 用户ID。
     * @param isbn   图书的ISBN。
     * @return 如果存在有效的预约记录，返回1；否则返回0。
     */
    @Select("""
        SELECT COUNT(*)
        FROM reservation_records
        WHERE userId = #{userId} AND isbn = #{isbn} AND status='ACTIVE'
        """)
    int existsActiveByUserAndIsbn(@Param("userId") String userId, @Param("isbn") String isbn);

    /**
     * 查询特定用户和特定ISBN之间的所有预约记录。
     *
     * @param userId 用户ID。
     * @param isbn   图书的ISBN。
     * @return 预约记录列表，按预约日期降序排列。
     */
    @Select("""
        SELECT * FROM reservation_records
        WHERE userId = #{userId} AND isbn = #{isbn}
        ORDER BY reserveDate DESC
        """)
    List<ReservationRecord> findByUserAndIsbn(@Param("userId") String userId, @Param("isbn") String isbn);

    /**
     * 查询所有的预约记录。
     *
     * @return 数据库中所有预约记录的列表。
     */
    @Select("SELECT * FROM reservation_records")
    List<ReservationRecord> findAll();

    /**
     * 根据状态查询预约记录。
     *
     * @param status 要查询的状态。
     * @return 具有指定状态的所有预约记录列表。
     */
    @Select("SELECT * FROM reservation_records WHERE status = #{status}")
    List<ReservationRecord> findByStatus(String status);

    /**
     * 更新指定预约记录的状态。
     *
     * @param reservationId 预约记录ID。
     * @param status        新的状态。
     * @return 受影响的行数。
     */
    @Update("""
    UPDATE reservation_records
    SET status = #{status}
    WHERE reservationId = #{reservationId}
    """)
    int updateStatus(@Param("reservationId") String reservationId,
                     @Param("status") String status);
}
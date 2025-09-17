package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BorrowRecord;

import java.util.List;

/**
 * 图书借阅记录Mapper接口。
 * <p>
 * 定义了与数据库中borrow_records表相关的操作。
 */
@Mapper
public interface BorrowRecordMapper {

    /**
     * 插入一条新的借阅记录。
     *
     * @param record 要插入的借阅记录对象。
     * @return 受影响的行数。
     */
    @Insert("""
        INSERT INTO borrow_records
        (recordId, userId, bookId, borrowDate, dueDate, returnDate, renewCount, status)
        VALUES
        (#{recordId}, #{userId}, #{bookId}, #{borrowDate}, #{dueDate}, #{returnDate}, #{renewCount}, #{status})
        """)
    int insert(BorrowRecord record);

    /**
     * 更新一条借阅记录的完整信息。
     *
     * @param record 包含更新信息的借阅记录对象。
     * @return 受影响的行数。
     */
    @Update("""
        UPDATE borrow_records
        SET userId=#{userId}, bookId=#{bookId}, borrowDate=#{borrowDate}, dueDate=#{dueDate},
            returnDate=#{returnDate}, renewCount=#{renewCount}, status=#{status}
        WHERE recordId=#{recordId}
        """)
    int update(BorrowRecord record);

    /**
     * 根据记录ID删除一条借阅记录。
     *
     * @param recordId 要删除的记录ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM borrow_records WHERE recordId=#{recordId}")
    int delete(String recordId);

    /**
     * 根据记录ID查询借阅记录。
     *
     * @param recordId 记录ID。
     * @return 对应的借阅记录对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM borrow_records WHERE recordId=#{recordId}")
    BorrowRecord findById(String recordId);

    /**
     * 根据用户ID查询其所有的借阅记录。
     *
     * @param userId 用户ID。
     * @return 该用户的所有借阅记录列表。
     */
    @Select("SELECT * FROM borrow_records WHERE userId=#{userId}")
    List<BorrowRecord> findByUserId(String userId);

    /**
     * 根据图书副本ID查询其所有的借阅记录。
     *
     * @param bookId 图书副本ID。
     * @return 该副本的所有借阅记录列表。
     */
    @Select("SELECT * FROM borrow_records WHERE bookId=#{bookId}")
    List<BorrowRecord> findByBookId(String bookId);

    /**
     * 查询用户当前所有未归还的借阅记录 (状态为 'BORROWED' 或 'OVERDUE')。
     *
     * @param userId 用户ID。
     * @return 用户未归还的借阅记录列表。
     */
    @Select("""
        SELECT * FROM borrow_records
        WHERE userId=#{userId} AND status IN ('BORROWED','OVERDUE')
        """)
    List<BorrowRecord> findActiveByUserId(String userId);

    /**
     * 查询某个副本当前未归还的借阅记录 (状态为 'BORROWED' 或 'OVERDUE')。
     *
     * @param bookId 图书副本ID。
     * @return 该副本未归还的借阅记录列表。
     */
    @Select("""
        SELECT * FROM borrow_records
        WHERE bookId=#{bookId} AND status IN ('BORROWED','OVERDUE')
        """)
    List<BorrowRecord> findActiveByBookId(String bookId);

    /**
     * 归还图书。
     * <p>
     * 将记录状态更新为 'RETURNED' 并记录归还日期。
     * 仅当原状态为 'BORROWED' 或 'OVERDUE' 时操作生效。
     *
     * @param recordId   要归还的借阅记录ID。
     * @param returnDate 实际归还日期。
     * @return 受影响的行数（1表示成功，0表示失败）。
     */
    @Update("""
        UPDATE borrow_records
        SET returnDate=#{returnDate}, status='RETURNED'
        WHERE recordId=#{recordId} AND status IN ('BORROWED','OVERDUE')
        """)
    int returnBook(@Param("recordId") String recordId, @Param("returnDate") String returnDate);

    /**
     * 续借图书。
     * <p>
     * 更新到期日，增加续借次数，并将状态重置为 'BORROWED'。
     * 允许对已逾期 ('OVERDUE') 的记录进行续借。
     *
     * @param recordId 要续借的记录ID。
     * @param dueDate  新的到期日期。
     * @return 受影响的行数。
     */
    @Update("""
        UPDATE borrow_records
        SET dueDate=#{dueDate}, renewCount = renewCount + 1, status='BORROWED'
        WHERE recordId=#{recordId} AND status IN ('BORROWED','OVERDUE')
        """)
    int renewBook(@Param("recordId") String recordId, @Param("dueDate") String dueDate);

    /**
     * 查询特定用户和特定图书副本之间的所有借阅记录。
     *
     * @param userId 用户ID。
     * @param bookId 图书副本ID。
     * @return 借阅记录列表，按借阅日期降序排列。
     */
    @Select("""
        SELECT * FROM borrow_records
        WHERE userId = #{userId} AND bookId = #{bookId}
        ORDER BY borrowDate DESC
        """)
    List<BorrowRecord> findByUserAndBook(@Param("userId") String userId, @Param("bookId") String bookId);

    /**
     * 统计用户当前未归还的借阅数量，用于借阅上限校验。
     *
     * @param userId 用户ID。
     * @return 用户未归还的图书数量。
     */
    @Select("""
        SELECT COUNT(*)
        FROM borrow_records
        WHERE userId = #{userId} AND status IN ('BORROWED','OVERDUE')
        """)
    int countActiveByUser(String userId);

    /**
     * 查询某个副本最新的一条未归还记录，常用于判断该副本是否被占用。
     *
     * @param bookId 图书副本ID。
     * @return 最新的未归还借阅记录，如果不存在则返回null。
     */
    @Select("""
        SELECT * FROM borrow_records
        WHERE bookId=#{bookId} AND status IN ('BORROWED','OVERDUE')
        ORDER BY borrowDate DESC
        LIMIT 1
        """)
    BorrowRecord findLatestActiveByBookId(String bookId);

    /**
     * 查询所有的借阅记录。
     *
     * @return 数据库中所有借阅记录的列表。
     */
    @Select("SELECT * FROM borrow_records")
    List<BorrowRecord> findAll();

    /**
     * 批量将已到期且未归还的记录标记为逾期。
     * <p>
     * 可用于定时任务，传入当前日期，将所有到期日早于当前日期的 'BORROWED' 记录更新为 'OVERDUE'。
     *
     * @param today 当前日期的字符串表示。
     * @return 受影响的行数。
     */
    @Update("""
        UPDATE borrow_records
        SET status='OVERDUE'
        WHERE status='BORROWED' AND dueDate < #{today}
        """)
    int markOverdueByDate(@Param("today") String today);
}
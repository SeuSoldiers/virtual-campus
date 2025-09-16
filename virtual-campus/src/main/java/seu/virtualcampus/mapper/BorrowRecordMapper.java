package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BorrowRecord;

import java.util.List;

@Mapper
public interface BorrowRecordMapper {

    @Insert("""
        INSERT INTO borrow_records
        (recordId, userId, bookId, borrowDate, dueDate, returnDate, renewCount, status)
        VALUES
        (#{recordId}, #{userId}, #{bookId}, #{borrowDate}, #{dueDate}, #{returnDate}, #{renewCount}, #{status})
        """)
    int insert(BorrowRecord record);

    @Update("""
        UPDATE borrow_records
        SET userId=#{userId}, bookId=#{bookId}, borrowDate=#{borrowDate}, dueDate=#{dueDate},
            returnDate=#{returnDate}, renewCount=#{renewCount}, status=#{status}
        WHERE recordId=#{recordId}
        """)
    int update(BorrowRecord record);

    @Delete("DELETE FROM borrow_records WHERE recordId=#{recordId}")
    int delete(String recordId);

    @Select("SELECT * FROM borrow_records WHERE recordId=#{recordId}")
    BorrowRecord findById(String recordId);

    @Select("SELECT * FROM borrow_records WHERE userId=#{userId}")
    List<BorrowRecord> findByUserId(String userId);

    @Select("SELECT * FROM borrow_records WHERE bookId=#{bookId}")
    List<BorrowRecord> findByBookId(String bookId);

    /** 用户的“进行中”借阅（含 BORROWED/OVERDUE） */
    @Select("""
        SELECT * FROM borrow_records
        WHERE userId=#{userId} AND status IN ('BORROWED','OVERDUE')
        """)
    List<BorrowRecord> findActiveByUserId(String userId);

    /** 某个副本的“进行中”借阅（含 BORROWED/OVERDUE） */
    @Select("""
        SELECT * FROM borrow_records
        WHERE bookId=#{bookId} AND status IN ('BORROWED','OVERDUE')
        """)
    List<BorrowRecord> findActiveByBookId(String bookId);

    /** 归还：写入归还时间并置为 RETURNED（仅当原状态为 BORROWED/OVERDUE 时生效） */
    @Update("""
        UPDATE borrow_records
        SET returnDate=#{returnDate}, status='RETURNED'
        WHERE recordId=#{recordId} AND status IN ('BORROWED','OVERDUE')
        """)
    int returnBook(@Param("recordId") String recordId, @Param("returnDate") String returnDate);

    /**
     * 续借：更新到期日、续借次数+1，并将状态置为 BORROWED
     * 允许对 OVERDUE 进行续借（把它拉回 BORROWED）
     */
    @Update("""
        UPDATE borrow_records
        SET dueDate=#{dueDate}, renewCount = renewCount + 1, status='BORROWED'
        WHERE recordId=#{recordId} AND status IN ('BORROWED','OVERDUE')
        """)
    int renewBook(@Param("recordId") String recordId, @Param("dueDate") String dueDate);

    @Select("""
        SELECT * FROM borrow_records
        WHERE userId = #{userId} AND bookId = #{bookId}
        ORDER BY borrowDate DESC
        """)
    List<BorrowRecord> findByUserAndBook(@Param("userId") String userId, @Param("bookId") String bookId);

    /** 某用户当前“进行中”借阅数量（做借阅上限校验用） */
    @Select("""
        SELECT COUNT(*)
        FROM borrow_records
        WHERE userId = #{userId} AND status IN ('BORROWED','OVERDUE')
        """)
    int countActiveByUser(String userId);

    /** 某副本当前一条活跃借阅（常用于判断该副本是否被占用） */
    @Select("""
        SELECT * FROM borrow_records
        WHERE bookId=#{bookId} AND status IN ('BORROWED','OVERDUE')
        ORDER BY borrowDate DESC
        LIMIT 1
        """)
    BorrowRecord findLatestActiveByBookId(String bookId);

    @Select("SELECT * FROM borrow_records")
    List<BorrowRecord> findAll();

    /** 批量标记逾期（可在定时任务中用；传入今天日期字符串，格式与库内一致） */
    @Update("""
        UPDATE borrow_records
        SET status='OVERDUE'
        WHERE status='BORROWED' AND dueDate < #{today}
        """)
    int markOverdueByDate(@Param("today") String today);
}

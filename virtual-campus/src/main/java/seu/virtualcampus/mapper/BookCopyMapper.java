package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BookCopy;
import java.util.List;
import java.util.Map;

@Mapper
public interface BookCopyMapper {

    @Insert("INSERT INTO book_copy(bookId, isbn, location, status) " +
            "VALUES(#{bookId}, #{isbn}, #{location}, #{status})")
    void insert(BookCopy bookCopy);

    @Update("UPDATE book_copy SET location=#{location}, status=#{status} WHERE bookId=#{bookId}")
    void update(BookCopy bookCopy);

    @Delete("DELETE FROM book_copy WHERE bookId=#{bookId}")
    void delete(String bookId);

    @Select("SELECT * FROM book_copy WHERE bookId=#{bookId}")
    BookCopy findById(String bookId);

    @Select("SELECT * FROM book_copy WHERE isbn=#{isbn}")
    List<BookCopy> findByIsbn(String isbn);

    @Select("SELECT * FROM book_copy WHERE status='IN_LIBRARY'")
    List<BookCopy> findAvailableCopies();

    // 自动统计数量：totalCount, availableCount, reservationCount
    @Select("""
        SELECT 
            COUNT(*) AS totalCount,
            SUM(CASE WHEN status = 'IN_LIBRARY' THEN 1 ELSE 0 END) AS availableCount,
            SUM(CASE WHEN status = 'RESERVED' THEN 1 ELSE 0 END) AS reservationCount
        FROM book_copy
        WHERE isbn = #{isbn}
        """)
    Map<String, Object> getCountsByIsbn(String isbn);

    // 借出一本书（状态改为 BORROWED）
    @Update("UPDATE book_copy SET status='BORROWED' " +
            "WHERE bookId=#{bookId} AND status='IN_LIBRARY'")
    int borrowBook(String bookId);

    // 归还一本书（状态改为 IN_LIBRARY）
    @Update("UPDATE book_copy SET status='IN_LIBRARY' " +
            "WHERE bookId=#{bookId} AND status='BORROWED'")
    int returnBook(String bookId);

    // 预约一本书（状态改为 RESERVED）
    @Update("UPDATE book_copy SET status='RESERVED' " +
            "WHERE bookId=#{bookId} AND status='IN_LIBRARY'")
    int reserveBook(String bookId);

    // 取消预约（状态改为 IN_LIBRARY）
    @Update("UPDATE book_copy SET status='IN_LIBRARY' " +
            "WHERE bookId=#{bookId} AND status='RESERVED'")
    int cancelReservation(String bookId);
}

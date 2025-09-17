package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BookCopy;
import java.util.List;
import java.util.Map;

/**
 * 图书副本Mapper接口。
 * <p>
 * 定义了与数据库中book_copy表相关的操作。
 */
@Mapper
public interface BookCopyMapper {

    /**
     * 插入一个新的图书副本记录。
     *
     * @param bookCopy 要插入的图书副本对象。
     */
    @Insert("INSERT INTO book_copy(bookId, isbn, location, status) " +
            "VALUES(#{bookId}, #{isbn}, #{location}, #{status})")
    void insert(BookCopy bookCopy);

    /**
     * 更新一个图书副本的信息。
     *
     * @param bookCopy 包含更新信息的图书副本对象。
     */
    @Update("UPDATE book_copy SET location=#{location}, status=#{status} WHERE bookId=#{bookId}")
    void update(BookCopy bookCopy);

    /**
     * 根据副本ID删除一个图书副本。
     *
     * @param bookId 要删除的副本ID。
     */
    @Delete("DELETE FROM book_copy WHERE bookId=#{bookId}")
    void delete(String bookId);

    /**
     * 根据副本ID查询图书副本信息。
     *
     * @param bookId 副本ID。
     * @return 对应的图书副本对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM book_copy WHERE bookId=#{bookId}")
    BookCopy findById(String bookId);

    /**
     * 根据ISBN查询所有相关的图书副本。
     *
     * @param isbn 图书的ISBN。
     * @return 该ISBN对应的所有副本列表。
     */
    @Select("SELECT * FROM book_copy WHERE isbn=#{isbn}")
    List<BookCopy> findByIsbn(String isbn);

    /**
     * 查询所有状态为'IN_LIBRARY'（在馆）的图书副本。
     *
     * @return 所有在馆可借的副本列表。
     */
    @Select("SELECT * FROM book_copy WHERE status='IN_LIBRARY'")
    List<BookCopy> findAvailableCopies();

    /**
     * 根据ISBN统计相关副本的数量信息。
     *
     * @param isbn 图书的ISBN。
     * @return 包含totalCount, availableCount, reservationCount的Map。
     */
    @Select("""
        SELECT 
            COUNT(*) AS totalCount,
            SUM(CASE WHEN status = 'IN_LIBRARY' THEN 1 ELSE 0 END) AS availableCount,
            SUM(CASE WHEN status = 'RESERVED' THEN 1 ELSE 0 END) AS reservationCount
        FROM book_copy
        WHERE isbn = #{isbn}
        """)
    Map<String, Object> getCountsByIsbn(String isbn);

    /**
     * 借出一本书。
     * <p>
     * 将指定副本ID的图书状态从'IN_LIBRARY'更新为'BORROWED'。
     * 这是一个原子操作，只有当图书状态为'IN_LIBRARY'时才会成功。
     *
     * @param bookId 要借出的图书副本ID。
     * @return 受影响的行数（1表示成功，0表示失败）。
     */
    @Update("UPDATE book_copy SET status='BORROWED' " +
            "WHERE bookId=#{bookId} AND status='IN_LIBRARY'")
    int borrowBook(String bookId);

    /**
     * 归还一本书。
     * <p>
     * 将指定副本ID的图书状态从'BORROWED'更新为'IN_LIBRARY'。
     *
     * @param bookId 要归还的图书副本ID。
     * @return 受影响的行数。
     */
    @Update("UPDATE book_copy SET status='IN_LIBRARY' " +
            "WHERE bookId=#{bookId} AND status='BORROWED'")
    int returnBook(String bookId);

    /**
     * 预约一本书。
     * <p>
     * 将指定副本ID的图书状态从'IN_LIBRARY'更新为'RESERVED'。
     *
     * @param bookId 要预约的图书副本ID。
     * @return 受影响的行数。
     */
    @Update("UPDATE book_copy SET status='RESERVED' " +
            "WHERE bookId=#{bookId} AND status='IN_LIBRARY'")
    int reserveBook(String bookId);

    /**
     * 取消一本书的预约。
     * <p>
     * 将指定副本ID的图书状态从'RESERVED'更新为'IN_LIBRARY'。
     *
     * @param bookId 要取消预约的图书副本ID。
     * @return 受影响的行数。
     */
    @Update("UPDATE book_copy SET status='IN_LIBRARY' " +
            "WHERE bookId=#{bookId} AND status='RESERVED'")
    int cancelReservation(String bookId);
}
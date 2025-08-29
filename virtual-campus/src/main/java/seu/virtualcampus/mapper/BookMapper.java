package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Book;
import java.util.List;

@Mapper
public interface BookMapper {
    @Insert("INSERT INTO books(bookId, title, author, isbn, category, publishDate, publisher, totalCount, availableCount, location, reservationCount) " +
            "VALUES(#{bookId}, #{title}, #{author}, #{isbn}, #{category}, #{publishDate}, #{publisher}, #{totalCount}, #{availableCount}, #{location}, #{reservationCount})")
    void insert(Book book);

    @Update("UPDATE books SET title=#{title}, author=#{author}, isbn=#{isbn}, category=#{category}, publishDate=#{publishDate}, " +
            "publisher=#{publisher}, totalCount=#{totalCount}, availableCount=#{availableCount}, location=#{location}, " +
            "reservationCount=#{reservationCount} WHERE bookId=#{bookId}")
    void update(Book book);

    @Delete("DELETE FROM books WHERE bookId=#{bookId}")
    void delete(String bookId);

    @Select("SELECT * FROM books WHERE bookId=#{bookId}")
    Book findById(String bookId);

    @Select("SELECT * FROM books WHERE title LIKE '%'|| #{title}|| '%'")
    List<Book> findByTitle(String title);

    @Select("SELECT * FROM books WHERE author LIKE '%'|| #{author}||'%'")
    List<Book> findByAuthor(String author);

    @Select("SELECT * FROM books WHERE category=#{category}")
    List<Book> findByCategory(String category);

    @Select("SELECT * FROM books WHERE availableCount > 0")
    List<Book> findAvailableBooks();

    @Update("UPDATE books SET availableCount = availableCount - 1 WHERE bookId = #{bookId} AND availableCount > 0")
    int decreaseAvailableCount(String bookId);

    @Update("UPDATE books SET availableCount = availableCount + 1 WHERE bookId = #{bookId}")
    int increaseAvailableCount(String bookId);

    @Update("UPDATE books SET reservationCount = reservationCount + 1 WHERE bookId = #{bookId}")
    int increaseReservationCount(String bookId);

    @Update("UPDATE books SET reservationCount = reservationCount - 1 WHERE bookId = #{bookId} AND reservationCount > 0")
    int decreaseReservationCount(String bookId);

    @Select("SELECT * FROM books WHERE isbn = #{isbn}")
    Book findByIsbn(String isbn);

    @Select("SELECT * FROM books")
    List<Book> findAll();
}
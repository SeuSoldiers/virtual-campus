package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BookInfo;

import java.util.List;

@Mapper
public interface BookInfoMapper {

    @Insert("INSERT INTO book_info(isbn, title, author, publisher, category, publishDate) " +
            "VALUES(#{isbn}, #{title}, #{author}, #{publisher}, #{category}, #{publishDate})")
    void insert(BookInfo bookInfo);

    @Update("UPDATE book_info SET title=#{title}, author=#{author}, publisher=#{publisher}, " +
            "category=#{category}, publishDate=#{publishDate} WHERE isbn=#{isbn}")
    void update(BookInfo bookInfo);

    @Delete("DELETE FROM book_info WHERE isbn=#{isbn}")
    void delete(String isbn);

    @Select("SELECT * FROM book_info WHERE isbn=#{isbn}")
    BookInfo findByIsbn(String isbn);

    @Select("SELECT * FROM book_info WHERE title LIKE '%' || #{title} || '%'")
    List<BookInfo> findByTitle(String title);

    @Select("SELECT * FROM book_info WHERE author LIKE '%' || #{author} || '%'")
    List<BookInfo> findByAuthor(String author);

    @Select("SELECT * FROM book_info WHERE category=#{category}")
    List<BookInfo> findByCategory(String category);

    @Select("SELECT * FROM book_info")
    List<BookInfo> findAll();
}

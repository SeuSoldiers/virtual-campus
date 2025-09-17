package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BookInfo;

import java.util.List;

/**
 * 图书信息Mapper接口。
 * <p>
 * 定义了与数据库中book_info表相关的操作。
 */
@Mapper
public interface BookInfoMapper {

    /**
     * 插入一条新的图书信息记录。
     *
     * @param bookInfo 要插入的图书信息对象。
     */
    @Insert("INSERT INTO book_info(isbn, title, author, publisher, category, publishDate) " +
            "VALUES(#{isbn}, #{title}, #{author}, #{publisher}, #{category}, #{publishDate})")
    void insert(BookInfo bookInfo);

    /**
     * 更新一条图书信息记录。
     *
     * @param bookInfo 包含更新信息的图书对象。
     */
    @Update("UPDATE book_info SET title=#{title}, author=#{author}, publisher=#{publisher}, " +
            "category=#{category}, publishDate=#{publishDate} WHERE isbn=#{isbn}")
    void update(BookInfo bookInfo);

    /**
     * 根据ISBN删除一条图书信息记录。
     *
     * @param isbn 要删除的图书的ISBN。
     */
    @Delete("DELETE FROM book_info WHERE isbn=#{isbn}")
    void delete(String isbn);

    /**
     * 根据ISBN查询图书信息。
     *
     * @param isbn 图书的ISBN。
     * @return 对应的图书信息对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM book_info WHERE isbn=#{isbn}")
    BookInfo findByIsbn(String isbn);

    /**
     * 根据书名模糊查询图书信息。
     *
     * @param title 书名关键词。
     * @return 匹配的图书信息列表。
     */
    @Select("SELECT * FROM book_info WHERE title LIKE '%' || #{title} || '%'")
    List<BookInfo> findByTitle(String title);

    /**
     * 根据作者名模糊查询图书信息。
     *
     * @param author 作者名关键词。
     * @return 匹配的图书信息列表。
     */
    @Select("SELECT * FROM book_info WHERE author LIKE '%' || #{author} || '%'")
    List<BookInfo> findByAuthor(String author);

    /**
     * 根据分类查询图书信息。
     *
     * @param category 图书分类。
     * @return 该分类下的所有图书信息列表。
     */
    @Select("SELECT * FROM book_info WHERE category=#{category}")
    List<BookInfo> findByCategory(String category);

    /**
     * 查询所有的图书信息。
     *
     * @return 数据库中所有图书信息的列表。
     */
    @Select("SELECT * FROM book_info")
    List<BookInfo> findAll();
}
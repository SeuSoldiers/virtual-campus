package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.BookInfo;
import seu.virtualcampus.mapper.BookCopyMapper;
import seu.virtualcampus.mapper.BookInfoMapper;
import seu.virtualcampus.mapper.ReservationRecordMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书信息服务类。
 * <p>
 * 提供图书信息的增删改查、按条件检索及统计等业务逻辑。
 * </p>
 */
@Service
public class BookInfoService {

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private BookCopyMapper bookCopyMapper;

    @Autowired
    private ReservationRecordMapper reservationRecordMapper;

    /**
     * 添加图书信息。
     *
     * @param bookInfo 图书信息对象。
     */
    public void addBook(BookInfo bookInfo) {
        bookInfoMapper.insert(bookInfo);
    }

    /**
     * 更新图书信息。
     *
     * @param bookInfo 图书信息对象。
     */
    public void updateBook(BookInfo bookInfo) {
        bookInfoMapper.update(bookInfo);
    }

    /**
     * 删除指定ISBN的图书。
     *
     * @param isbn 图书ISBN。
     */
    public void deleteBook(String isbn) {
        bookInfoMapper.delete(isbn);
    }

    /**
     * 根据ISBN获取图书信息。
     *
     * @param isbn 图书ISBN。
     * @return 对应的图书信息对象，若不存在则返回null。
     */
    public BookInfo getBookByIsbn(String isbn) {
        BookInfo book = bookInfoMapper.findByIsbn(isbn);
        if (book != null) {
            attachCounts(book);
        }
        return book;
    }

    /**
     * 按标题模糊搜索图书。
     *
     * @param title 图书标题。
     * @return 匹配标题的图书列表。
     */
    public List<BookInfo> searchBooksByTitle(String title) {
        return enrichWithCounts(bookInfoMapper.findByTitle(title));
    }

    /**
     * 按作者模糊搜索图书。
     *
     * @param author 作者名。
     * @return 匹配作者的图书列表。
     */
    public List<BookInfo> searchBooksByAuthor(String author) {
        return enrichWithCounts(bookInfoMapper.findByAuthor(author));
    }

    /**
     * 按分类搜索图书。
     *
     * @param category 分类名。
     * @return 匹配分类的图书列表。
     */
    public List<BookInfo> searchBooksByCategory(String category) {
        return enrichWithCounts(bookInfoMapper.findByCategory(category));
    }

    /**
     * 获取所有图书信息。
     *
     * @return 所有图书信息列表。
     */
    public List<BookInfo> getAllBooks() {
        return enrichWithCounts(bookInfoMapper.findAll());
    }

    /**
     * 批量为 BookInfo 附加统计数量
     */
    private List<BookInfo> enrichWithCounts(List<BookInfo> books) {
        return books.stream().map(book -> {
            attachCounts(book);
            return book;
        }).collect(Collectors.toList());
    }

    /**
     * 为单本图书统计副本数量并填充。
     *
     * @param book 图书信息对象。
     */
    private void attachCounts(BookInfo book) {
        Map<String, Object> counts = bookCopyMapper.getCountsByIsbn(book.getIsbn());
        int total = 0;
        int available = 0;
        if (counts != null) {
            Object totalObj = counts.get("totalCount");
            Object availObj = counts.get("availableCount");
            total = (totalObj == null ? 0 : ((Number) totalObj).intValue());
            available = (availObj == null ? 0 : ((Number) availObj).intValue());
        }

        int reserved = reservationRecordMapper.countActiveByIsbn(book.getIsbn());

        book.setTotalCount(total);
        book.setAvailableCount(available);
        book.setReservationCount(reserved);
    }

    public BookInfo refreshBookByIsbn(String isbn) {
        BookInfo book = bookInfoMapper.findByIsbn(isbn);
        if (book != null) {
            attachCounts(book);
        }
        return book;
    }
}

package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.BookInfo;
import seu.virtualcampus.mapper.BookInfoMapper;
import seu.virtualcampus.mapper.BookCopyMapper;
import seu.virtualcampus.mapper.ReservationRecordMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookInfoService {

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private BookCopyMapper bookCopyMapper;

    @Autowired
    private ReservationRecordMapper reservationRecordMapper;

    public void addBook(BookInfo bookInfo) {
        bookInfoMapper.insert(bookInfo);
    }

    public void updateBook(BookInfo bookInfo) {
        bookInfoMapper.update(bookInfo);
    }

    public void deleteBook(String isbn) {
        bookInfoMapper.delete(isbn);
    }

    public BookInfo getBookByIsbn(String isbn) {
        BookInfo book = bookInfoMapper.findByIsbn(isbn);
        if (book != null) {
            attachCounts(book);
        }
        return book;
    }

    public List<BookInfo> searchBooksByTitle(String title) {
        return enrichWithCounts(bookInfoMapper.findByTitle(title));
    }

    public List<BookInfo> searchBooksByAuthor(String author) {
        return enrichWithCounts(bookInfoMapper.findByAuthor(author));
    }

    public List<BookInfo> searchBooksByCategory(String category) {
        return enrichWithCounts(bookInfoMapper.findByCategory(category));
    }

    public List<BookInfo> getAllBooks() {
        return enrichWithCounts(bookInfoMapper.findAll());
    }

    /** 批量为 BookInfo 附加统计数量 */
    private List<BookInfo> enrichWithCounts(List<BookInfo> books) {
        return books.stream().map(book -> {
            attachCounts(book);
            return book;
        }).collect(Collectors.toList());
    }

    /** 给单本书计算数量并填充 */
    private void attachCounts(BookInfo book) {
        // 1. 副本数
        Map<String, Object> counts = bookCopyMapper.getCountsByIsbn(book.getIsbn());
        int total = 0;
        int available = 0;
        if (counts != null) {
            Object totalObj = counts.get("totalCount");
            Object availObj = counts.get("availableCount");
            total = (totalObj == null ? 0 : ((Number) totalObj).intValue());
            available = (availObj == null ? 0 : ((Number) availObj).intValue());
        }

        // 2. 预约数（只算 ACTIVE）
        int reserved = reservationRecordMapper.countActiveByIsbn(book.getIsbn());

        // 3. 写回 BookInfo
        book.setTotalCount(total);
        book.setAvailableCount(available);
        book.setReservationCount(reserved);
    }

    /** 对外提供刷新后的 BookInfo（按 ISBN 查） */
    public BookInfo refreshBookByIsbn(String isbn) {
        BookInfo book = bookInfoMapper.findByIsbn(isbn);
        if (book != null) {
            attachCounts(book);
        }
        return book;
    }
}

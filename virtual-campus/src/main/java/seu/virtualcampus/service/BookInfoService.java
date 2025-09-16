package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.BookInfo;
import seu.virtualcampus.mapper.BookInfoMapper;
import seu.virtualcampus.mapper.BookCopyMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookInfoService {

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private BookCopyMapper bookCopyMapper;

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
        Map<String, Object> counts = bookCopyMapper.getCountsByIsbn(book.getIsbn());
        if (counts != null) {
            Object total = counts.get("totalCount");
            Object available = counts.get("availableCount");
            Object reserved = counts.get("reservationCount");

            book.setTotalCount(total == null ? 0 : ((Number) total).intValue());
            book.setAvailableCount(available == null ? 0 : ((Number) available).intValue());
            book.setReservationCount(reserved == null ? 0 : ((Number) reserved).intValue());
        } else {
            book.setTotalCount(0);
            book.setAvailableCount(0);
            book.setReservationCount(0);
        }
    }
}

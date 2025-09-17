package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.BookCopy;
import seu.virtualcampus.mapper.BookCopyMapper;

import java.util.List;

/**
 * 图书副本服务类。
 * <p>
 * 提供对图书副本的增删改查、借阅、归还、预约等相关业务逻辑。
 * </p>
 */
@Service
public class BookCopyService {

    @Autowired
    private BookCopyMapper bookCopyMapper;

    /**
     * 添加图书副本。
     *
     * @param copy 图书副本对象。
     */
    public void addCopy(BookCopy copy) {
        bookCopyMapper.insert(copy);
    }

    /**
     * 更新图书副本信息。
     *
     * @param copy 图书副本对象。
     */
    public void updateCopy(BookCopy copy) {
        bookCopyMapper.update(copy);
    }

    /**
     * 删除指定ID的图书副本。
     *
     * @param bookId 图书副本ID。
     */
    public void deleteCopy(String bookId) {
        bookCopyMapper.delete(bookId);
    }

    /**
     * 根据ID获取图书副本。
     *
     * @param bookId 图书副本ID。
     * @return 对应的图书副本对象，若不存在则返回null。
     */
    public BookCopy getCopyById(String bookId) {
        return bookCopyMapper.findById(bookId);
    }

    /**
     * 根据ISBN获取所有副本。
     *
     * @param isbn 图书ISBN。
     * @return 该ISBN下的所有副本列表。
     */
    public List<BookCopy> getCopiesByIsbn(String isbn) {
        return bookCopyMapper.findByIsbn(isbn);
    }

    /**
     * 获取所有可借阅的副本。
     *
     * @return 可借阅副本列表。
     */
    public List<BookCopy> getAvailableCopies() {
        return bookCopyMapper.findAvailableCopies();
    }

    /**
     * 借阅副本（bookId）。
     *
     * @param bookId 图书副本ID。
     * @return 借阅成功返回true，否则返回false。
     */
    @Transactional
    public boolean borrowBook(String bookId) {
        return bookCopyMapper.borrowBook(bookId) > 0;
    }

    /**
     * 归还副本（bookId）。
     *
     * @param bookId 图书副本ID。
     * @return 归还成功返回true，否则返回false。
     */
    @Transactional
    public boolean returnBook(String bookId) {
        return bookCopyMapper.returnBook(bookId) > 0;
    }

    /**
     * 预约副本（bookId → RESERVED）。
     *
     * @param bookId 图书副本ID。
     * @return 预约成功返回true，否则返回false。
     */
    @Transactional
    public boolean reserveBook(String bookId) {
        return bookCopyMapper.reserveBook(bookId) > 0;
    }

    /**
     * 取消预约（bookId → IN_LIBRARY）。
     *
     * @param bookId 图书副本ID。
     * @return 取消预约成功返回true，否则返回false。
     */
    @Transactional
    public boolean cancelReservation(String bookId) {
        return bookCopyMapper.cancelReservation(bookId) > 0;
    }
}

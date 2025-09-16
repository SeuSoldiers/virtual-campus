package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.BookCopy;
import seu.virtualcampus.mapper.BookCopyMapper;

import java.util.List;

@Service
public class BookCopyService {

    @Autowired
    private BookCopyMapper bookCopyMapper;

    public void addCopy(BookCopy copy) {
        bookCopyMapper.insert(copy);
    }

    public void updateCopy(BookCopy copy) {
        bookCopyMapper.update(copy);
    }

    public void deleteCopy(String bookId) {
        bookCopyMapper.delete(bookId);
    }

    public BookCopy getCopyById(String bookId) {
        return bookCopyMapper.findById(bookId);
    }

    public List<BookCopy> getCopiesByIsbn(String isbn) {
        return bookCopyMapper.findByIsbn(isbn);
    }

    public List<BookCopy> getAvailableCopies() {
        return bookCopyMapper.findAvailableCopies();
    }

    /** 借阅副本（bookId） */
    @Transactional
    public boolean borrowBook(String bookId) {
        return bookCopyMapper.borrowBook(bookId) > 0;
    }

    /** 归还副本（bookId） */
    @Transactional
    public boolean returnBook(String bookId) {
        return bookCopyMapper.returnBook(bookId) > 0;
    }

    /** 预约副本（bookId → RESERVED） */
    @Transactional
    public boolean reserveBook(String bookId) {
        return bookCopyMapper.reserveBook(bookId) > 0;
    }

    /** 取消预约（bookId → IN_LIBRARY） */
    @Transactional
    public boolean cancelReservation(String bookId) {
        return bookCopyMapper.cancelReservation(bookId) > 0;
    }
}

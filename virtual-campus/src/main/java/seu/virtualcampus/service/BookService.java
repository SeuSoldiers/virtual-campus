package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.Book;
import seu.virtualcampus.mapper.BookMapper;
import java.util.List;

@Service
public class BookService {
    @Autowired
    private BookMapper bookMapper;

    public void addBook(Book book) {
        bookMapper.insert(book);
    }

    public void updateBook(Book book) {
        bookMapper.update(book);
    }

    public void deleteBook(String bookId) {
        bookMapper.delete(bookId);
    }

    public Book getBookById(String bookId) {
        return bookMapper.findById(bookId);
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookMapper.findByTitle(title);
    }

    public List<Book> searchBooksByAuthor(String author) {
        return bookMapper.findByAuthor(author);
    }

    public List<Book> searchBooksByCategory(String category) {
        return bookMapper.findByCategory(category);
    }

    public List<Book> getAvailableBooks() {
        return bookMapper.findAvailableBooks();
    }

    @Transactional
    public boolean borrowBook(String bookId) {
        int result = bookMapper.decreaseAvailableCount(bookId);
        return result > 0;
    }

    @Transactional
    public void returnBook(String bookId) {
        bookMapper.increaseAvailableCount(bookId);
    }

    @Transactional
    public void increaseReservationCount(String bookId) {
        bookMapper.increaseReservationCount(bookId);
    }

    @Transactional
    public void decreaseReservationCount(String bookId) {
        bookMapper.decreaseReservationCount(bookId);
    }

    public Book getBookByIsbn(String isbn) {
        return bookMapper.findByIsbn(isbn);
    }

    public List<Book> getAllBooks() {
        return bookMapper.findAll();
    }
}
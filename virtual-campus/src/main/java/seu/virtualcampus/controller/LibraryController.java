package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Book;
import seu.virtualcampus.domain.BorrowRecord;
import seu.virtualcampus.domain.ReservationRecord;
import seu.virtualcampus.service.BookService;
import seu.virtualcampus.service.BorrowRecordService;
import seu.virtualcampus.service.ReservationRecordService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private ReservationRecordService reservationRecordService;

    /**
     * 1. 查询图书 - 服务端函数
     */
    @GetMapping("/books")
    public ResponseEntity<List<Book>> queryBooksApi(@RequestParam Map<String, String> queryParams) {
        List<Book> books = queryBooks(queryParams);
        return ResponseEntity.ok(books);
    }


    public List<Book> queryBooks(Map<String, String> queryParams) {
        String title = queryParams.get("title");
        String author = queryParams.get("author");
        String category = queryParams.get("category");
        String isbn = queryParams.get("isbn");

        if (title != null && !title.isEmpty()) {
            return bookService.searchBooksByTitle(title);
        } else if (author != null && !author.isEmpty()) {
            return bookService.searchBooksByAuthor(author);
        } else if (category != null && !category.isEmpty()) {
            return bookService.searchBooksByCategory(category);
        } else if (isbn != null && !isbn.isEmpty()) {
            // 这里需要添加根据ISBN查询的方法
            Book book = bookService.getBookByIsbn(isbn);
            return book != null ? List.of(book) : List.of();
        } else {
            // 如果没有指定条件，返回所有可用图书
            return bookService.getAvailableBooks();
        }
    }

    /**
     * 2. 借阅图书 - 服务端函数
     */

    @PostMapping("/borrow")
    public ResponseEntity<Map<String, Object>> borrowBookApi(
            @RequestParam String userId,
            @RequestParam String bookId) {
        Map<String, Object> result = borrowBook(userId, bookId);
        return ResponseEntity.ok(result);
    }

    public Map<String, Object> borrowBook(String userId, String bookId) {
        BorrowRecord record = borrowRecordService.borrowBook(userId, bookId);

        if (record != null) {
            return Map.of(
                    "success", true,
                    "message", "借阅成功",
                    "recordId", record.getRecordId()
            );
        } else {
            return Map.of(
                    "success", false,
                    "message", "图书不可借"
            );
        }
    }

    /**
     * 3. 归还图书 - 服务端函数
     */

    @PostMapping("/return")
    public ResponseEntity<Map<String, Object>> returnBookApi(@RequestParam String recordId) {
        Map<String, Object> result = returnBook(recordId);
        return ResponseEntity.ok(result);
    }

    public Map<String, Object> returnBook(String recordId) {
        try {
            borrowRecordService.returnBook(recordId);
            return Map.of(
                    "success", true,
                    "message", "归还成功"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "归还失败: " + e.getMessage()
            );
        }
    }

    /**
     * 4. 续借图书 - 服务端函数
     */
    @PostMapping("/renew")
    public ResponseEntity<Map<String, Object>> renewBookApi(@RequestParam String recordId) {
        Map<String, Object> result = renewBook(recordId);
        return ResponseEntity.ok(result);
    }

    public Map<String, Object> renewBook(String recordId) {
        boolean success = borrowRecordService.renewBook(recordId);

        if (success) {
            // 获取更新后的借阅记录以获取新的到期日期
            BorrowRecord record = borrowRecordService.getBorrowRecordById(recordId);
            return Map.of(
                    "success", true,
                    "message", "续借成功",
                    "newDueDate", record.getDueDate().toString()
            );
        } else {
            return Map.of(
                    "success", false,
                    "message", "续借失败，可能已达到最大续借次数"
            );
        }
    }

    /**
     * 5. 获取借阅记录 - 服务端函数
     */
    @GetMapping("/borrow-records")
    public ResponseEntity<List<BorrowRecord>> queryBorrowRecordsApi(
            @RequestParam Map<String, String> queryParams) {
        List<BorrowRecord> records = queryBorrowRecords(queryParams);
        return ResponseEntity.ok(records);
    }

    public List<BorrowRecord> queryBorrowRecords(Map<String, String> queryParams) {
        String userId = queryParams.get("userId");
        String bookId = queryParams.get("bookId");
        String status = queryParams.get("status");

        if (userId != null && bookId != null) {
            // 根据用户和图书查询
            return borrowRecordService.getBorrowRecordsByUserAndBook(userId, bookId);
        } else if (userId != null) {
            // 根据用户查询
            if ("ACTIVE".equals(status)) {
                return borrowRecordService.getActiveBorrowRecordsByUser(userId);
            } else {
                return borrowRecordService.getBorrowRecordsByUser(userId);
            }
        } else if (bookId != null) {
            // 根据图书查询
            return borrowRecordService.getBorrowRecordsByBook(bookId);
        } else {
            // 返回所有借阅记录
            return borrowRecordService.getAllBorrowRecords();
        }
    }

    /**
     * 6. 管理图书 - 服务端函数
     */
    @PostMapping("/books/manage")
    public ResponseEntity<String> manageBooksApi(
            @RequestBody Book book,
            @RequestParam String action) {
        String result = manageBooks(book, action);
        return ResponseEntity.ok(result);
    }


    public String manageBooks(Book book, String action) {
        try {
            if ("add".equals(action)) {
                bookService.addBook(book);
                return "图书添加成功";
            } else if ("update".equals(action)) {
                bookService.updateBook(book);
                return "图书更新成功";
            } else if ("delete".equals(action)) {
                bookService.deleteBook(book.getBookId());
                return "图书删除成功";
            } else {
                return "未知操作";
            }
        } catch (Exception e) {
            return "操作失败: " + e.getMessage();
        }
    }

    /**
     * 7. 预约图书 - 服务端函数
     */
    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Object>> reserveBookApi(
            @RequestParam String userId,
            @RequestParam String bookId) {
        Map<String, Object> result = reserveBook(userId, bookId);
        return ResponseEntity.ok(result);
    }

    public Map<String, Object> reserveBook(String userId, String bookId) {
        try {
            ReservationRecord record = reservationRecordService.reserveBook(userId, bookId);
            return Map.of(
                    "success", true,
                    "message", "预约成功",
                    "reservationId", record.getReservationId(),
                    "queuePosition", record.getQueuePosition()
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "预约失败: " + e.getMessage()
            );
        }
    }

    /**
     * 8. 取消预约 - 服务端函数
     */
    @PostMapping("/reserve/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservationApi(@RequestParam String reservationId) {
        Map<String, Object> result = cancelReservation(reservationId);
        return ResponseEntity.ok(result);
    }

    public Map<String, Object> cancelReservation(String reservationId) {
        try {
            reservationRecordService.cancelReservation(reservationId);
            return Map.of(
                    "success", true,
                    "message", "取消预约成功"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "取消预约失败: " + e.getMessage()
            );
        }
    }

    /**
     * 9. 查询预约记录 - 服务端函数
     */
    @GetMapping("/reserve-records")
    public ResponseEntity<List<ReservationRecord>> queryReservationRecordsApi(
            @RequestParam Map<String, String> queryParams) {
        List<ReservationRecord> records = queryReservationRecords(queryParams);
        return ResponseEntity.ok(records);
    }


    public List<ReservationRecord> queryReservationRecords(Map<String, String> queryParams) {
        String userId = queryParams.get("userId");
        String bookId = queryParams.get("bookId");

        if (userId != null && bookId != null) {
            // 根据用户和图书查询预约记录
            return reservationRecordService.getReservationsByUserAndBook(userId, bookId);
        } else if (userId != null) {
            // 根据用户查询预约记录
            return reservationRecordService.getUserReservations(userId);
        } else if (bookId != null) {
            // 根据图书查询预约记录
            return reservationRecordService.getActiveReservationsByBook(bookId);
        } else {
            // 返回所有预约记录
            return reservationRecordService.getAllReservations();
        }
    }
}
package seu.virtualcampus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.controller.LibraryController;
import seu.virtualcampus.domain.Book;
import seu.virtualcampus.domain.BorrowRecord;
import seu.virtualcampus.domain.ReservationRecord;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

//@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
@SpringBootTest
@Transactional
@Rollback
public class   LibraryIntegrationTest {

    @Autowired
    private LibraryController libraryController;

    @Test
    public void testCompleteBorrowProcess() {
        // 1. 查询图书
        Map<String, String> query = Map.of("title", "Java");
        List<Book> books = libraryController.queryBooks(query);
        assertNotNull(books);
        assertEquals(1, books.size());
        Book book = books.get(0);
        assertEquals(3, book.getAvailableCount());

        // 2. 借阅图书
        Map<String, Object> borrowResult = libraryController.borrowBook("U006", "B001");
        assertTrue((Boolean) borrowResult.get("success"));
        String recordId = (String) borrowResult.get("recordId");

        // 3. 验证图书可用数量减少
        books = libraryController.queryBooks(query);
        assertEquals(2, books.get(0).getAvailableCount()); // 原来是3，借出1本后为2

        // 4. 获取借阅记录
        Map<String, String> borrowQuery = Map.of("userId", "U006");
        List<BorrowRecord> records = libraryController.queryBorrowRecords(borrowQuery);
        assertEquals(1, records.size());
        assertEquals("BORROWED", records.get(0).getStatus());

        // 5. 预约同一本书
        Map<String, Object> reserveResult = libraryController.reserveBook("U007", "B001");
        assertTrue((Boolean) reserveResult.get("success"));
        int queuePosition = (Integer) reserveResult.get("queuePosition");
        assertEquals(3, queuePosition); // 原来有2个预约，新增后为第3个

        // 6. 归还图书
        Map<String, Object> returnResult = libraryController.returnBook(recordId);
        assertTrue((Boolean) returnResult.get("success"));

        // 7. 验证图书可用数量恢复
        books = libraryController.queryBooks(query);
        assertEquals(3, books.get(0).getAvailableCount()); // 归还后恢复3本

        // 8. 检查预约记录
        Map<String, String> reserveQuery = Map.of("bookId", "B001");
        List<ReservationRecord> reservations = libraryController.queryReservationRecords(reserveQuery);
        assertEquals(3, reservations.size()); // 包括新添加的预约
    }
}
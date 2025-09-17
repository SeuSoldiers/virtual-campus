package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.BookInfo;
import seu.virtualcampus.domain.BookCopy;
import seu.virtualcampus.domain.BorrowRecord;
import seu.virtualcampus.domain.ReservationRecord;
import seu.virtualcampus.service.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    @Autowired private BookInfoService bookInfoService;
    @Autowired private BookCopyService bookCopyService;
    @Autowired private BorrowRecordService borrowRecordService;
    @Autowired private ReservationRecordService reservationRecordService;

    // ==================== 搜索/详情 ====================

    @GetMapping("/search")
    public ResponseEntity<List<BookInfo>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String category) {
        if (isbn != null && !isbn.isEmpty()) {
            BookInfo book = bookInfoService.getBookByIsbn(isbn);
            return ResponseEntity.ok(book == null ? List.of() : List.of(book));
        } else if (title != null && !title.isEmpty()) {
            return ResponseEntity.ok(bookInfoService.searchBooksByTitle(title));
        } else if (author != null && !author.isEmpty()) {
            return ResponseEntity.ok(bookInfoService.searchBooksByAuthor(author));
        } else if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(bookInfoService.searchBooksByCategory(category));
        } else {
            return ResponseEntity.ok(bookInfoService.getAllBooks());
        }
    }

    @GetMapping("/{isbn}/copies")
    public ResponseEntity<List<BookCopy>> getBookCopies(@PathVariable String isbn) {
        return ResponseEntity.ok(bookCopyService.getCopiesByIsbn(isbn));
    }

    // ==================== 借阅 ====================

    @PostMapping("/borrow")
    public ResponseEntity<String> borrowBook(@RequestParam String userId,
                                             @RequestParam String bookId) {
        // 校验用户是否还能借
        if (!borrowRecordService.canBorrow(userId, 30)) {
            return ResponseEntity.badRequest().body("已达借阅上限，无法借阅更多图书");
        }
        // 借阅副本
        boolean success = bookCopyService.borrowBook(bookId);
        if (!success) {
            return ResponseEntity.badRequest().body("借阅失败，该副本不可借");
        }
        // 写入借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(borrowRecordService.generateRecordId());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusMonths(1));
        record.setRenewCount(0);
        record.setStatus("BORROWED");
        borrowRecordService.addBorrowRecord(record);

        String isbn = resolveIsbnByBookId(bookId);
        if (isbn != null) bookInfoService.refreshBookByIsbn(isbn);

        return ResponseEntity.ok("借阅成功");
    }

    // ==================== 归还 ====================

    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestParam String recordId,
                                             @RequestParam String bookId,
                                             @RequestParam String isbn) {
        // 归还借阅记录
        boolean recordOk = borrowRecordService.returnBook(recordId, LocalDate.now().toString());
        if (!recordOk) {
            return ResponseEntity.badRequest().body("归还失败，借阅记录无效");
        }
        // 归还副本
        bookCopyService.returnBook(bookId);
        bookInfoService.refreshBookByIsbn(isbn);

        // 检查是否有预约队列
        ReservationRecord next = reservationRecordService.getFirstActiveByIsbn(isbn);
        if (next != null) {
            // 直接保留副本 IN_LIBRARY 状态
            return ResponseEntity.ok("归还成功，该书已有预约用户，可以去兑现");
        }
        return ResponseEntity.ok("归还成功");
    }

    // ==================== 预约 ====================

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveBook(@RequestParam String userId,
                                              @RequestParam String isbn) {
        // 检查是否有可借副本
        List<BookCopy> copies = bookCopyService.getCopiesByIsbn(isbn);
        boolean hasAvailable = copies.stream().anyMatch(c -> "IN_LIBRARY".equals(c.getStatus()));
        if (hasAvailable) {
            return ResponseEntity.badRequest().body("该书有可借副本，请直接借阅，无需预约");
        }
        // 插入预约
        ReservationRecord record = new ReservationRecord();
        record.setReservationId(reservationRecordService.generateReservationId());
        record.setUserId(userId);
        record.setIsbn(isbn);
        record.setReserveDate(LocalDate.now());
        record.setStatus("ACTIVE");

        boolean ok = reservationRecordService.addReservation(record);
        if (ok) bookInfoService.refreshBookByIsbn(isbn);
        return ok ? ResponseEntity.ok("预约成功") : ResponseEntity.badRequest().body("您已预约过该书");
    }

    @PostMapping("/cancel-reservation")
    public ResponseEntity<String> cancelReservation(@RequestParam String reservationId) {
        boolean ok = reservationRecordService.cancelReservation(reservationId);
        if (ok) {
            ReservationRecord res = reservationRecordService.getById(reservationId);
            if (res != null) bookInfoService.refreshBookByIsbn(res.getIsbn());
        }
        return ok ? ResponseEntity.ok("取消预约成功") : ResponseEntity.badRequest().body("取消失败");
    }

    // ==================== 预约兑现 ====================

    @PostMapping("/fulfill")
    public ResponseEntity<String> fulfillReservation(@RequestParam String reservationId,
                                                     @RequestParam String userId,
                                                     @RequestParam String bookId) {
        ReservationRecord res = reservationRecordService.getById(reservationId);
        if (res == null) {
            return ResponseEntity.badRequest().body("预约不存在");
        }

        // 只允许 ACTIVE 状态兑现
        if (!"ACTIVE".equalsIgnoreCase(res.getStatus())) {
            return ResponseEntity.badRequest().body("预约无效或已处理");
        }

        // 标记预约为已兑现
        boolean fulfilled = reservationRecordService.fulfillReservation(reservationId);
        if (!fulfilled) {
            return ResponseEntity.badRequest().body("预约兑现失败");
        }

        // 改副本为借出
        boolean success = bookCopyService.borrowBook(bookId);
        if (!success) {
            return ResponseEntity.badRequest().body("借阅失败，副本状态错误");
        }

        // 写入借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(borrowRecordService.generateRecordId());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusMonths(1));
        record.setRenewCount(0);
        record.setStatus("BORROWED");
        borrowRecordService.addBorrowRecord(record);

        String isbn = resolveIsbnByBookId(bookId);
        if (isbn != null) bookInfoService.refreshBookByIsbn(isbn);

        return ResponseEntity.ok("预约兑现成功，图书已借出");
    }

    // ==================== 管理员接口 ====================

    @PostMapping("/admin/book")
    public ResponseEntity<String> addBook(@RequestBody BookInfo bookInfo) {
        bookInfoService.addBook(bookInfo);
        return ResponseEntity.ok("图书添加成功");
    }

    @PutMapping("/admin/book")
    public ResponseEntity<String> updateBook(@RequestBody BookInfo bookInfo) {
        bookInfoService.updateBook(bookInfo);
        return ResponseEntity.ok("图书更新成功");
    }

    @DeleteMapping("/admin/book/{isbn}")
    public ResponseEntity<String> deleteBook(@PathVariable String isbn) {
        bookInfoService.deleteBook(isbn);
        return ResponseEntity.ok("图书删除成功");
    }

    @PostMapping("/admin/copy")
    public ResponseEntity<String> addCopy(@RequestBody BookCopy copy) {
        bookCopyService.addCopy(copy);
        return ResponseEntity.ok("副本添加成功");
    }

    @PutMapping("/admin/copy")
    public ResponseEntity<String> updateCopy(@RequestBody BookCopy copy) {
        bookCopyService.updateCopy(copy);
        return ResponseEntity.ok("副本更新成功");
    }

    @DeleteMapping("/admin/copy/{bookId}")
    public ResponseEntity<String> deleteCopy(@PathVariable String bookId) {
        bookCopyService.deleteCopy(bookId);
        return ResponseEntity.ok("副本删除成功");
    }

    @GetMapping("/admin/borrows")
    public ResponseEntity<List<AdminBorrowItemDTO>> adminListBorrows(
            @RequestParam(required = false) String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        List<BorrowRecord> all = borrowRecordService.getAll();

        List<AdminBorrowItemDTO> dto = all.stream()
                .map(br -> {
                    AdminBorrowItemDTO d = new AdminBorrowItemDTO();
                    d.recordId = br.getRecordId();
                    d.bookId = br.getBookId();
                    d.title = resolveTitleByBookId(br.getBookId());
                    d.userId = br.getUserId();
                    d.borrowDate = br.getBorrowDate();
                    d.returnDate = br.getReturnDate();
                    d.status = br.getStatus();
                    return d;
                })
                .filter(d -> kw.isEmpty()
                        || (d.recordId != null && d.recordId.toLowerCase().contains(kw))
                        || (d.bookId != null && d.bookId.toLowerCase().contains(kw))
                        || (d.title != null && d.title.toLowerCase().contains(kw))
                        || (d.userId != null && d.userId.toLowerCase().contains(kw)))
                .sorted(Comparator.comparing((AdminBorrowItemDTO d) ->
                        d.borrowDate == null ? LocalDate.MIN : d.borrowDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<AdminReservationItemDTO>> adminListReservations(
            @RequestParam(required = false) String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        List<ReservationRecord> all = reservationRecordService.getAll();

        List<AdminReservationItemDTO> dto = all.stream()
                .map(r -> {
                    AdminReservationItemDTO d = new AdminReservationItemDTO();
                    d.reservationId = r.getReservationId();
                    d.isbn = r.getIsbn();
                    d.title = resolveTitleByIsbn(r.getIsbn());
                    d.userId = r.getUserId();
                    d.reserveDate = r.getReserveDate();
                    d.queuePosition = r.getQueuePosition() == null ? 0 : r.getQueuePosition();
                    d.status = r.getStatus();
                    return d;
                })
                .filter(d -> kw.isEmpty()
                        || (d.reservationId != null && d.reservationId.toLowerCase().contains(kw))
                        || (d.isbn != null && d.isbn.toLowerCase().contains(kw))
                        || (d.title != null && d.title.toLowerCase().contains(kw))
                        || (d.userId != null && d.userId.toLowerCase().contains(kw)))
                .sorted(Comparator.comparing((AdminReservationItemDTO d) ->
                        d.reserveDate == null ? LocalDate.MIN : d.reserveDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }
    // ==================== 学生视图：当前借阅 / 借阅历史 / 预约记录 ====================

    @GetMapping("/borrows/current")
    public ResponseEntity<List<BorrowItemDTO>> getCurrentBorrows(@RequestParam String userId) {
        // 进行中 = BORROWED 或 OVERDUE
        List<BorrowRecord> list = borrowRecordService.getActiveByUser(userId);
        List<BorrowItemDTO> dto = list.stream().map(br -> {
            String title = resolveTitleByBookId(br.getBookId());
            BorrowItemDTO d = new BorrowItemDTO();
            d.recordId = br.getRecordId();
            d.bookId = br.getBookId();
            d.title = title;
            d.isbn = bookCopyService.getCopyById(br.getBookId()).getIsbn();
            d.borrowDate = br.getBorrowDate();
            d.dueDate = br.getDueDate();
            d.status = br.getStatus();
            return d;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/borrows/history")
    public ResponseEntity<List<BorrowHistoryItemDTO>> getBorrowHistory(@RequestParam String userId) {
        // 历史 = 已归还
        List<BorrowRecord> all = borrowRecordService.getByUser(userId);
        List<BorrowHistoryItemDTO> dto = all.stream()
                .filter(br -> "RETURNED".equals(br.getStatus()))
                .sorted((a, b) -> {
                    // 按 returnDate DESC
                    java.time.LocalDate ra = a.getReturnDate();
                    java.time.LocalDate rb = b.getReturnDate();
                    if (ra == null && rb == null) return 0;
                    if (ra == null) return 1;
                    if (rb == null) return -1;
                    return rb.compareTo(ra);
                })
                .map(br -> {
                    String title = resolveTitleByBookId(br.getBookId());
                    BorrowHistoryItemDTO d = new BorrowHistoryItemDTO();
                    d.recordId = br.getRecordId();
                    d.bookId = br.getBookId();
                    d.title = title;
                    d.borrowDate = br.getBorrowDate();
                    d.returnDate = br.getReturnDate();
                    d.status = br.getStatus();
                    return d;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationItemDTO>> getReservations(@RequestParam String userId) {
        List<ReservationRecord> list = reservationRecordService.getByUser(userId);
        // 默认按预约时间倒序展示
        List<ReservationItemDTO> dto = list.stream()
                .sorted((a, b) -> {
                    java.time.LocalDate ra = a.getReserveDate();
                    java.time.LocalDate rb = b.getReserveDate();
                    if (ra == null && rb == null) return 0;
                    if (ra == null) return 1;
                    if (rb == null) return -1;
                    return rb.compareTo(ra);
                })
                .map(r -> {
                    String title = resolveTitleByIsbn(r.getIsbn());
                    ReservationItemDTO d = new ReservationItemDTO();
                    d.reservationId = r.getReservationId();
                    d.isbn = r.getIsbn();
                    d.title = title;
                    d.reserveDate = r.getReserveDate();
                    d.queuePosition = r.getQueuePosition() == null ? 0 : r.getQueuePosition();
                    d.status = r.getStatus();
                    return d;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/reservation/my")
    public ResponseEntity<Map<String, Object>> getMyReservation(
            @RequestParam String userId,
            @RequestParam String isbn) {

        // 用 service 查出该用户对该 ISBN 的“非取消”最新记录（ACTIVE/NOTIFIED 等）
        List<ReservationRecord> all = reservationRecordService.getByUser(userId);
        ReservationRecord r = all.stream()
                .filter(x -> isbn.equals(x.getIsbn()))
                .filter(x -> !"CANCELLED".equalsIgnoreCase(x.getStatus()))
                .sorted(Comparator.comparing(ReservationRecord::getReserveDate).reversed())
                .findFirst().orElse(null);

        if (r == null) return ResponseEntity.notFound().build();
        Map<String,Object> dto = new HashMap<>();
        dto.put("reservationId", r.getReservationId());
        dto.put("userId", r.getUserId());
        dto.put("isbn", r.getIsbn());
        dto.put("status", r.getStatus());
        return ResponseEntity.ok(dto);
    }

    /** —— 以下是轻量 DTO，与前端 BorrowViewController 对齐 —— */
    public static class BorrowItemDTO {
        public String recordId;
        public String bookId;
        public String title;
        public String isbn;
        public java.time.LocalDate borrowDate;
        public java.time.LocalDate dueDate;
        public String status;
    }
    public static class BorrowHistoryItemDTO {
        public String recordId;
        public String bookId;
        public String title;
        public java.time.LocalDate borrowDate;
        public java.time.LocalDate returnDate;
        public String status;
    }
    public static class ReservationItemDTO {
        public String reservationId;
        public String isbn;
        public String title;
        public java.time.LocalDate reserveDate;
        public Integer queuePosition;
        public String status;
    }
    public static class AdminBorrowItemDTO {
        public String recordId;
        public String bookId;
        public String title;
        public String userId;
        public String userName;
        public java.time.LocalDate borrowDate;
        public java.time.LocalDate returnDate;
        public String status;
    }
    public static class AdminReservationItemDTO {
        public String reservationId;
        public String isbn;
        public String title;
        public String userId;
        public String userName;
        public java.time.LocalDate reserveDate;
        public Integer queuePosition;
        public String status;
    }

    /** 工具：bookId → title */
    private String resolveTitleByBookId(String bookId) {
        BookCopy copy = bookCopyService.getCopyById(bookId);
        if (copy == null) return "";
        BookInfo info = bookInfoService.getBookByIsbn(copy.getIsbn());
        return info == null ? "" : (info.getTitle() == null ? "" : info.getTitle());
    }

    /** 工具：isbn → title */
    private String resolveTitleByIsbn(String isbn) {
        BookInfo info = bookInfoService.getBookByIsbn(isbn);
        return info == null ? "" : (info.getTitle() == null ? "" : info.getTitle());
    }

    /** 工具：bookId → isbn */
    private String resolveIsbnByBookId(String bookId) {
        BookCopy copy = bookCopyService.getCopyById(bookId);
        return copy == null ? null : copy.getIsbn();
    }
}

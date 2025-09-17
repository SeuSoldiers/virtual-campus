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

/**
 * 图书馆控制器。
 * <p>
 * 提供图书馆相关的所有API接口，包括图书搜索、借阅、归还、预约等面向用户的操作，
 * 以及图书信息和副本管理的管理员操作。
 */
@RestController
@RequestMapping("/api/library")
public class LibraryController {

    @Autowired private BookInfoService bookInfoService;
    @Autowired private BookCopyService bookCopyService;
    @Autowired private BorrowRecordService borrowRecordService;
    @Autowired private ReservationRecordService reservationRecordService;

    /**
     * 搜索图书信息。
     * <p>
     * 支持通过书名、作者、ISBN或分类进行搜索。如果不提供任何参数，则返回所有图书。
     *
     * @param title    书名（可选）。
     * @param author   作者（可选）。
     * @param isbn     ISBN号（可选，优先级最高）。
     * @param category 分类（可选）。
     * @return 符合条件的图书信息列表。
     */
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

    /**
     * 根据ISBN获取一本书的所有副本信息。
     *
     * @param isbn 图书的ISBN号。
     * @return 该书的所有副本列表。
     */
    @GetMapping("/{isbn}/copies")
    public ResponseEntity<List<BookCopy>> getBookCopies(@PathVariable String isbn) {
        return ResponseEntity.ok(bookCopyService.getCopiesByIsbn(isbn));
    }

    /**
     * 借阅一本图书。
     *
     * @param userId 用户ID。
     * @param bookId 要借阅的图书副本ID。
     * @return 操作结果的消息。
     */
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestParam String userId, @RequestParam String bookId) {
        try {
            BookCopy copy = bookCopyService.getCopyById(bookId);
            if (copy == null) {
                return ResponseEntity.badRequest().body("借阅失败：副本不存在");
            }
            if (!"IN_LIBRARY".equalsIgnoreCase(copy.getStatus())) {
                return ResponseEntity.badRequest().body("借阅失败：该副本不可借（当前状态：" + copy.getStatus() + "）");
            }

            ReservationRecord firstRes = reservationRecordService.getFirstActiveByIsbn(copy.getIsbn());
            if (firstRes != null) {
                // 如果当前用户就是队首预约者，允许直接借阅
                if (!firstRes.getUserId().equals(userId)) {
                    return ResponseEntity.badRequest().body("该书已有预约，只有队首用户可兑付");
                }

                // 队首用户 -> 借阅成功，同时标记预约为 FULFILLED
                boolean fulfilled = reservationRecordService.fulfillReservation(firstRes.getReservationId());
                if (!fulfilled) {
                    return ResponseEntity.badRequest().body("预约兑现失败，请稍后再试");
                }
            }

            borrowRecordService.borrowBook(userId, bookId);

            boolean ok = bookCopyService.borrowBook(bookId);
            if (!ok) {
                throw new RuntimeException("借阅失败：副本状态更新失败");
            }

            bookInfoService.refreshBookByIsbn(copy.getIsbn());

            return ResponseEntity.ok("借书成功（30天）");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 续借一本图书。
     *
     * @param recordId 借阅记录的ID。
     * @return 操作结果的消息。
     */
    @PostMapping("/borrow/{recordId}/renew")
    public ResponseEntity<?> renewBorrow(@PathVariable String recordId) {
        try {
            borrowRecordService.renewBorrow(recordId);
            return ResponseEntity.ok("续借成功（+30天）");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 归还一本图书。
     *
     * @param recordId 借阅记录的ID。
     * @param bookId   归还的图书副本ID。
     * @param isbn     归还的图书的ISBN，用于检查预约队列。
     * @return 操作结果的消息。
     */
    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestParam String recordId,
                                             @RequestParam String bookId,
                                             @RequestParam String isbn) {
        boolean recordOk = borrowRecordService.returnBook(recordId, LocalDate.now().toString());
        if (!recordOk) {
            return ResponseEntity.badRequest().body("归还失败，借阅记录无效");
        }

        bookCopyService.returnBook(bookId);

        ReservationRecord next = reservationRecordService.getFirstActiveByIsbn(isbn);
        if (next != null) {
            return ResponseEntity.ok("归还成功，该书已有预约用户，可以去兑现");
        }

        return ResponseEntity.ok("归还成功");
    }

    /**
     * 预约一本图书。
     * <p>
     * 当一本书的所有副本都不可借时，用户可以进行预约。
     *
     * @param userId 用户ID。
     * @param isbn   要预约的图书的ISBN。
     * @return 操作结果的消息。
     */
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

    /**
     * 取消一个图书预约。
     *
     * @param reservationId 要取消的预约记录ID。
     * @return 操作结果的消息。
     */
    @PostMapping("/cancel-reservation")
    public ResponseEntity<String> cancelReservation(@RequestParam String reservationId) {
        boolean ok = reservationRecordService.cancelReservation(reservationId);
        if (ok) {
            ReservationRecord res = reservationRecordService.getById(reservationId);
            if (res != null) bookInfoService.refreshBookByIsbn(res.getIsbn());
        }
        return ok ? ResponseEntity.ok("取消预约成功") : ResponseEntity.badRequest().body("取消失败");
    }

    /**
     * 兑现一个图书预约。
     * <p>
     * 当有预约的书被归还后，此接口用于为预约队列中的用户办理借阅手续。
     *
     * @param reservationId 预约记录ID。
     * @param userId        用户ID。
     * @param bookId        为用户分配的图书副本ID。
     * @return 操作结果的消息。
     */
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
        try {
            borrowRecordService.borrowBook(userId, bookId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("借阅失败：" + e.getMessage());
        }

        String isbn = resolveIsbnByBookId(bookId);
        if (isbn != null) bookInfoService.refreshBookByIsbn(isbn);

        return ResponseEntity.ok("预约兑现成功，图书已借出");
    }

    // ==================== 管理员接口 ====================

    /**
     * (管理员) 添加一本新的图书信息。
     *
     * @param bookInfo 图书信息对象。
     * @return 操作结果的消息。
     */
    @PostMapping("/admin/book")
    public ResponseEntity<String> addBook(@RequestBody BookInfo bookInfo) {
        bookInfoService.addBook(bookInfo);
        return ResponseEntity.ok("图书添加成功");
    }

    /**
     * (管理员) 更新一本图书的信息。
     *
     * @param bookInfo 包含更新内容的图书信息对象。
     * @return 操作结果的消息。
     */
    @PutMapping("/admin/book")
    public ResponseEntity<String> updateBook(@RequestBody BookInfo bookInfo) {
        bookInfoService.updateBook(bookInfo);
        return ResponseEntity.ok("图书更新成功");
    }

    /**
     * (管理员) 根据ISBN删除一本图书及其所有副本。
     *
     * @param isbn 要删除的图书的ISBN。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/admin/book/{isbn}")
    public ResponseEntity<String> deleteBook(@PathVariable String isbn) {
        List<BookCopy> copies = bookCopyService.getCopiesByIsbn(isbn);
        for (BookCopy copy : copies) {
            bookCopyService.deleteCopy(copy.getBookId());
        }

        bookInfoService.deleteBook(isbn);

        return ResponseEntity.ok("图书删除成功");
    }


    /**
     * (管理员) 添加一个图书副本。
     *
     * @param copy 图书副本对象。
     * @return 操作结果的消息。
     */
    @PostMapping("/admin/copy")
    public ResponseEntity<String> addCopy(@RequestBody BookCopy copy) {
        bookCopyService.addCopy(copy);
        bookInfoService.refreshBookByIsbn(copy.getIsbn());
        return ResponseEntity.ok("副本添加成功");
    }

    /**
     * (管理员) 更新一个图书副本的信息。
     *
     * @param copy 包含更新内容的图书副本对象。
     * @return 操作结果的消息。
     */
    @PutMapping("/admin/copy")
    public ResponseEntity<String> updateCopy(@RequestBody BookCopy copy) {
        bookCopyService.updateCopy(copy);
        bookInfoService.refreshBookByIsbn(copy.getIsbn());
        return ResponseEntity.ok("副本更新成功");
    }

    /**
     * (管理员) 根据副本ID删除一个图书副本。
     *
     * @param bookId 要删除的图书副本ID。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/admin/copy/{bookId}")
    public ResponseEntity<String> deleteCopy(@PathVariable String bookId) {
        BookCopy copy = bookCopyService.getCopyById(bookId);
        bookCopyService.deleteCopy(bookId);
        if (copy != null) {
            bookInfoService.refreshBookByIsbn(copy.getIsbn());
        }
        return ResponseEntity.ok("副本删除成功");
    }


    /**
     * (管理员) 获取所有借阅记录，支持关键词搜索。
     *
     * @param keyword 搜索关键词（可选），可匹配记录ID、副本ID、书名或用户ID。
     * @return 经过滤和排序的借阅记录列表。
     */
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
                    d.dueDate = br.getDueDate();
                    d.returnDate = br.getReturnDate();
                    d.status = br.getStatus();
                    d.renewCount = br.getRenewCount() == null ? 0 : br.getRenewCount();
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

    /**
     * (管理员) 获取所有预约记录，支持关键词搜索。
     *
     * @param keyword 搜索关键词（可选），可匹配预约ID、ISBN、书名或用户ID。
     * @return 经过滤和排序的预约记录列表。
     */
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

    /**
     * 获取指定用户当前的借阅记录（未归还的）。
     *
     * @param userId 用户ID。
     * @return 当前借阅记录的列表。
     */
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
            d.renewCount = br.getRenewCount() == null ? 0 : br.getRenewCount();
            return d;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    /**
     * 获取指定用户的历史借阅记录（已归还的）。
     *
     * @param userId 用户ID。
     * @return 历史借阅记录的列表。
     */
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
                    d.renewCount = br.getRenewCount() == null ? 0 : br.getRenewCount();
                    return d;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    /**
     * 获取指定用户的所有预约记录。
     *
     * @param userId 用户ID。
     * @return 预约记录的列表。
     */
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

    /**
     * 获取用户对特定图书的有效预约记录。
     *
     * @param userId 用户ID。
     * @param isbn   图书的ISBN。
     * @return 最新的有效预约记录；如果不存在则返回404。
     */
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

    /** DTO for current borrows view. */
    public static class BorrowItemDTO {
        public String recordId;
        public String bookId;
        public String title;
        public String isbn;
        public java.time.LocalDate borrowDate;
        public java.time.LocalDate dueDate;
        public String status;
        public Integer renewCount;
    }
    /** DTO for borrow history view. */
    public static class BorrowHistoryItemDTO {
        public String recordId;
        public String bookId;
        public String title;
        public java.time.LocalDate borrowDate;
        public java.time.LocalDate returnDate;
        public String status;
        public Integer renewCount;
    }
    /** DTO for reservations view. */
    public static class ReservationItemDTO {
        public String reservationId;
        public String isbn;
        public String title;
        public java.time.LocalDate reserveDate;
        public Integer queuePosition;
        public String status;
    }
    /** DTO for admin borrows view. */
    public static class AdminBorrowItemDTO {
        public String recordId;
        public String bookId;
        public String title;
        public String userId;
        public java.time.LocalDate borrowDate;
        public java.time.LocalDate dueDate;
        public java.time.LocalDate returnDate;
        public String status;
        public Integer renewCount;
    }
    /** DTO for admin reservations view. */
    public static class AdminReservationItemDTO {
        public String reservationId;
        public String isbn;
        public String title;
        public String userId;
        public java.time.LocalDate reserveDate;
        public Integer queuePosition;
        public String status;
    }

    /**
     * 工具方法：通过图书副本ID解析出书名。
     *
     * @param bookId 图书副本ID。
     * @return 书名，如果找不到则返回空字符串。
     */
    private String resolveTitleByBookId(String bookId) {
        BookCopy copy = bookCopyService.getCopyById(bookId);
        if (copy == null) return "";
        BookInfo info = bookInfoService.getBookByIsbn(copy.getIsbn());
        return info == null ? "" : (info.getTitle() == null ? "" : info.getTitle());
    }

    /**
     * 工具方法：通过ISBN解析出书名。
     *
     * @param isbn 图书的ISBN。
     * @return 书名，如果找不到则返回空字符串。
     */
    private String resolveTitleByIsbn(String isbn) {
        BookInfo info = bookInfoService.getBookByIsbn(isbn);
        return info == null ? "" : (info.getTitle() == null ? "" : info.getTitle());
    }

    /**
     * 工具方法：通过图书副本ID解析出ISBN。
     *
     * @param bookId 图书副本ID。
     * @return ISBN，如果找不到则返回空字符串。
     */
    private String resolveIsbnByBookId(String bookId) {
        BookCopy copy = bookCopyService.getCopyById(bookId);
        return copy == null ? null : copy.getIsbn();
    }
}
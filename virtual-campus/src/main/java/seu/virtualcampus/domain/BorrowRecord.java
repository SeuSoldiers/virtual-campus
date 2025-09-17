package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 图书借阅记录实体类。
 * <p>
 * 记录了一次完整的图书借阅事件，包括借阅人、图书、时间等信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {
    /**
     * 借阅记录的唯一ID。
     */
    private String recordId;
    /**
     * 借阅者的用户ID。
     */
    private String userId;
    /**
     * 被借阅的图书副本ID。
     */
    private String bookId;
    /**
     * 借阅发生的日期。
     */
    private LocalDate borrowDate;
    /**
     * 应归还的截止日期。
     */
    private LocalDate dueDate;
    /**
     * 实际归还的日期。
     */
    private LocalDate returnDate;
    /**
     * 续借次数。
     */
    private Integer renewCount;
    /**
     * 借阅记录的当前状态。
     * <p>
     * 例如: "BORROWED" (已借出), "RETURNED" (已归还), "OVERDUE" (逾期未归还)。
     */
    private String status; // 状态如: BORROWED, RETURNED, OVERDUE
}
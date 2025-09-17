package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 图书预约记录实体类。
 * <p>
 * 记录了用户对某本图书的预约信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRecord {
    /**
     * 预约记录的唯一ID。
     */
    private String reservationId;
    /**
     * 预约用户的ID。
     */
    private String userId;
    /**
     * 被预约图书的ISBN号。
     */
    private String isbn;
    /**
     * 预约发生的日期。
     */
    private LocalDate reserveDate;
    /**
     * 预约记录的当前状态。
     * <p>
     * 例如: "ACTIVE" (有效), "CANCELLED" (已取消), "FULFILLED" (已兑现)。
     */
    private String status; // 状态如: ACTIVE, CANCELLED, FULFILLED
    /**
     * 用户在该书预约队列中的位置。
     */
    private Integer queuePosition;
}
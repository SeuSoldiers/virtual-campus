package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRecord {
    private String reservationId;
    private String userId;
    private String isbn;
    private LocalDate reserveDate;
    private String status; // 状态如: ACTIVE, CANCELLED, FULFILLED
    private Integer queuePosition;
}
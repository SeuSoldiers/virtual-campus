package seu.virtualcampus.domain;

import java.time.LocalDate;

public class ReservationRecord {
    private String reservationId;
    private String userId;
    private String bookId;
    private LocalDate reserveDate;
    private String status; // 状态如: ACTIVE, CANCELLED, FULFILLED
    private Integer queuePosition;
    private String notifyStatus; // 通知状态如: NOT_NOTIFIED, NOTIFIED

    // 默认构造方法
    public ReservationRecord() {
    }

    // 带参数的构造方法
    public ReservationRecord(String reservationId, String userId, String bookId,
                             LocalDate reserveDate, String status, Integer queuePosition,
                             String notifyStatus) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.bookId = bookId;
        this.reserveDate = reserveDate;
        this.status = status;
        this.queuePosition = queuePosition;
        this.notifyStatus = notifyStatus;
    }

    // Getter 和 Setter 方法
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public LocalDate getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(LocalDate reserveDate) {
        this.reserveDate = reserveDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public String getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(String notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    @Override
    public String toString() {
        return "ReservationRecord{" +
                "reservationId='" + reservationId + '\'' +
                ", userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", reserveDate=" + reserveDate +
                ", status='" + status + '\'' +
                ", queuePosition=" + queuePosition +
                ", notifyStatus='" + notifyStatus + '\'' +
                '}';
    }
}
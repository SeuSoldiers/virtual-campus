// Transaction.java
package seu.virtualcampus.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行交易记录实体类。
 * <p>
 * 代表一次银行账户间的资金流动，如存款、取款、转账等。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    /**
     * 交易的唯一ID。
     */
    private String transactionId;
    /**
     * 交易的转出方账户号码。
     */
    private String fromAccountNumber;
    /**
     * 交易的转入方账户号码。
     */
    private String toAccountNumber;
    /**
     * 交易金额。
     */
    private BigDecimal amount;
    /**
     * 交易类型。
     * <p>
     * 例如: "DEPOSIT" (存款), "WITHDRAWAL" (取款), "TRANSFER" (转账)。
     */
    private String transactionType;
    /**
     * 交易发生的时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;
    /**
     * 交易备注。
     */
    private String remark;
    /**
     * 交易状态。
     * <p>
     * 例如: "COMPLETED" (已完成), "PENDING" (处理中), "FAILED" (失败)。
     */
    private String status;
}
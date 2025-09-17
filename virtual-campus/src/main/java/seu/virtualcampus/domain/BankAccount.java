// BankAccount.java
package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行账户实体类。
 * <p>
 * 代表一个用户的银行账户信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {
    /**
     * 账户的唯一号码。
     */
    private String accountNumber;    // 账户唯一号码
    /**
     * 账户所属用户的ID。
     */
    private String userId;           // 所属用户ID
    /**
     * 账户的交易密码。
     */
    private String password;         // 账户密码（新增字段）
    /**
     * 账户类型。
     * <p>
     * 例如: "USER" (普通用户), "ADMINISTRATOR" (管理员)。
     */
    private String accountType;      // 账户类型（USER/ADMINISTRATOR）
    /**
     * 账户的当前余额。
     */
    private BigDecimal balance;      // 账户余额
    /**
     * 账户的状态。
     * <p>
     * 例如: "ACTIVE" (正常), "BLOCKED" (冻结), "CLOSED" (已关闭), "LIMIT" (受限)。
     */
    private String status;           // 账户状态（ACTIVE/BLOCKED/CLOSED/LIMIT：【后期定期计划考虑：OVERDRAWN/RESTRICTED】）
    /**
     * 账户的开户日期和时间。
     */
    private LocalDateTime createdDate;        // 开户日期（改为Date类型）
}
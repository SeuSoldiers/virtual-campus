// BankAccount.java
package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {
    private String accountNumber;    // 账户唯一号码
    private String userId;           // 所属用户ID
    private String password;         // 账户密码（新增字段）
    private String accountType;      // 账户类型（ACTIVE/BLOCKED/CLOSED：【后期定期计划考虑：OVERDRAWN/RESTRICTED】）
    private BigDecimal balance;      // 账户余额
    private String status;           // 账户状态
    private LocalDateTime createdDate;        // 开户日期（改为Date类型）
}
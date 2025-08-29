// BankAccount.java
package seu.virtualcampus.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccount {
    private String accountNumber;    // 账户唯一号码
    private String userId;           // 所属用户ID
    private String password;         // 账户密码（新增字段）
    private String accountType;      // 账户类型
    private BigDecimal balance;      // 账户余额
    private String status;           // 账户状态
    private LocalDateTime createdDate;        // 开户日期（改为Date类型）

    // 构造方法
    public BankAccount() {}

    public BankAccount(String accountNumber, String userId, String password,
                       String accountType, BigDecimal balance, String status, LocalDateTime createdDate) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.password = password;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdDate = createdDate;
    }

    // Getter和Setter方法
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        return "BankAccount{" +
                "accountNumber='" + accountNumber + '\'' +
                ", userId='" + userId + '\'' +
                ", password='" + password + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
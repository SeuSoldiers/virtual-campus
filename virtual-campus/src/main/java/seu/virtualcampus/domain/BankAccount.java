// BankAccount.java
package seu.virtualcampus.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccount {
    private String accountNumber;
    private String userId;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdDate;

    // 构造方法
    public BankAccount() {}

    public BankAccount(String accountNumber, String userId, String accountType,
                       BigDecimal balance, String status, LocalDateTime createdDate) {
        this.accountNumber = accountNumber;
        this.userId = userId;
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

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
// BankAccountService.java
package seu.virtualcampus.service;

import seu.virtualcampus.domain.BankAccount;
import seu.virtualcampus.domain.Transaction;
import seu.virtualcampus.mapper.BankAccountMapper;
import seu.virtualcampus.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BankAccountService {

    @Autowired
    private BankAccountMapper bankAccountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    // 创建新账户
    @Transactional
    public BankAccount createAccount(String userId, String accountType, BigDecimal initialDeposit) {
        String accountNumber = generateAccountNumber();

        BankAccount account = new BankAccount(
                accountNumber,
                userId,
                accountType,
                initialDeposit,
                "ACTIVE",
                LocalDateTime.now()
        );

        bankAccountMapper.insertAccount(account);

        // 记录初始存款交易
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction(
                    generateTransactionId(),
                    null,
                    accountNumber,
                    initialDeposit,
                    "DEPOSIT",
                    LocalDateTime.now(),
                    "Initial deposit",
                    "COMPLETED"
            );
            transactionMapper.insertTransaction(transaction);
        }

        return account;
    }

    // 处理存款
    @Transactional
    public Transaction processDeposit(String accountNumber, BigDecimal amount) {
        // 验证账户存在且有效
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        // 更新余额
        BigDecimal newBalance = account.getBalance().add(amount);
        bankAccountMapper.updateBalance(accountNumber, newBalance);

        // 创建交易记录
        Transaction transaction = new Transaction(
                generateTransactionId(),
                null,
                accountNumber,
                amount,
                "DEPOSIT",
                LocalDateTime.now(),
                "Deposit transaction",
                "COMPLETED"
        );
        transactionMapper.insertTransaction(transaction);

        return transaction;
    }

    // 处理取款
    @Transactional
    public Transaction processWithdrawal(String accountNumber, BigDecimal amount, String password) {
        // 验证账户存在且有效
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        // 验证密码（此处简化，实际应用中应加密存储和验证）
        // 假设密码验证通过

        // 检查余额是否充足
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // 更新余额
        BigDecimal newBalance = account.getBalance().subtract(amount);
        bankAccountMapper.updateBalance(accountNumber, newBalance);

        // 创建交易记录
        Transaction transaction = new Transaction(
                generateTransactionId(),
                accountNumber,
                null,
                amount,
                "WITHDRAWAL",
                LocalDateTime.now(),
                "Withdrawal transaction",
                "COMPLETED"
        );
        transactionMapper.insertTransaction(transaction);

        return transaction;
    }

    // 处理转账
    @Transactional
    public Transaction processTransfer(String fromAccount, String toAccount, BigDecimal amount, String password) {
        // 验证转出账户
        BankAccount from = bankAccountMapper.selectByAccountNumber(fromAccount);
        if (from == null) {
            throw new RuntimeException("From account not found");
        }
        if (!"ACTIVE".equals(from.getStatus())) {
            throw new RuntimeException("From account is not active");
        }

        // 验证转入账户
        BankAccount to = bankAccountMapper.selectByAccountNumber(toAccount);
        if (to == null) {
            throw new RuntimeException("To account not found");
        }
        if (!"ACTIVE".equals(to.getStatus())) {
            throw new RuntimeException("To account is not active");
        }

        // 验证密码（此处简化）
        // 假设密码验证通过

        // 检查余额是否充足
        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // 更新转出账户余额
        BigDecimal fromNewBalance = from.getBalance().subtract(amount);
        bankAccountMapper.updateBalance(fromAccount, fromNewBalance);

        // 更新转入账户余额
        BigDecimal toNewBalance = to.getBalance().add(amount);
        bankAccountMapper.updateBalance(toAccount, toNewBalance);

        // 创建交易记录
        Transaction transaction = new Transaction(
                generateTransactionId(),
                fromAccount,
                toAccount,
                amount,
                "TRANSFER",
                LocalDateTime.now(),
                "Transfer transaction",
                "COMPLETED"
        );
        transactionMapper.insertTransaction(transaction);

        return transaction;
    }

    // 获取余额
    public BigDecimal getAccountBalance(String accountNumber) {
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        return account.getBalance();
    }

    // 获取交易历史
    public List<Transaction> getTransactionHistory(String accountNumber, LocalDateTime start, LocalDateTime end) {
        return transactionMapper.selectByAccountNumberAndTimeRange(accountNumber, start, end);
    }

    // 更新账户状态
    public boolean updateAccountStatus(String accountNumber, String newStatus) {
        int result = bankAccountMapper.updateStatus(accountNumber, newStatus);
        return result > 0;
    }

    // 生成账户号码
    private String generateAccountNumber() {
        return "AC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // 生成交易ID
    private String generateTransactionId() {
        return "TX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
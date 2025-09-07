// BankAccountService.java (部分更新)
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
    public BankAccount createAccount(String userId, String accountType, String password,BigDecimal initialDeposit) {
        String accountNumber = generateAccountNumber();

        BankAccount account = new BankAccount(
                accountNumber,
                userId,
                password, // 需要设置默认密码或从参数获取
                accountType,
                initialDeposit,
                "ACTIVE",
                LocalDateTime.now()
        );

        bankAccountMapper.insertAccount(account); // 使用正确的方法名

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
                    "ACTIVE"
            );
            transactionMapper.insertTransaction(transaction);
        }

        return account;
    }

    // 处理存款
    @Transactional
    public Transaction processDeposit(String accountNumber, BigDecimal amount) {
        // 验证账户存在且有效
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber); // 使用正确的方法名
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        // +++ 新增：验证存款金额必须为正数 +++
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be > zero");
        }
        // +++ 结束新增 +++

        // 更新余额
        BigDecimal newBalance = account.getBalance().add(amount);
        bankAccountMapper.updateBalance(accountNumber, newBalance); // 使用正确的方法名

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
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber); // 使用正确的方法名
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        // 验证密码
        if (!account.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        // 检查余额是否充足
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // 更新余额
        BigDecimal newBalance = account.getBalance().subtract(amount);
        bankAccountMapper.updateBalance(accountNumber, newBalance); // 使用正确的方法名

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
        BankAccount from = bankAccountMapper.selectByAccountNumber(fromAccount); // 使用正确的方法名
        if (from == null) {
            throw new RuntimeException("From account not found");
        }
        if (!"ACTIVE".equals(from.getStatus())) {
            throw new RuntimeException("From account is not active");
        }

        // 验证密码
        if (!from.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        // 验证转入账户
        BankAccount to = bankAccountMapper.selectByAccountNumber(toAccount); // 使用正确的方法名
        if (to == null) {
            throw new RuntimeException("To account not found");
        }
        if (!"ACTIVE".equals(to.getStatus())) {
            throw new RuntimeException("To account is not active");
        }

        // 检查余额是否充足
        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // 更新转出账户余额
        BigDecimal fromNewBalance = from.getBalance().subtract(amount);
        bankAccountMapper.updateBalance(fromAccount, fromNewBalance); // 使用正确的方法名

        // 更新转入账户余额
        BigDecimal toNewBalance = to.getBalance().add(amount);
        bankAccountMapper.updateBalance(toAccount, toNewBalance); // 使用正确的方法名

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

    // 获取余额（***改为个人信息查询）
    public BankAccount getAccountInfo(String accountNumber) {
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber); // 使用正确的方法名
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        return account;
    }

    // 获取交易历史
    public List<Transaction> getTransactionHistory(String accountNumber, LocalDateTime start, LocalDateTime end) {
        return transactionMapper.selectByAccountNumberAndTimeRange(accountNumber, start, end);
    }

    // 更新账户状态
    public boolean updateAccountStatus(String accountNumber, String newStatus) {
        int result = bankAccountMapper.updateStatus(accountNumber, newStatus); // 使用正确的方法名
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

    // 验证账户密码(登录)
    public boolean verifyAccountPassword(String accountNumber, String password) {
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber); // 使用正确的方法名
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        return account.getPassword().equals(password);
    }

    // 更新账户密码：思考（能不能把验证账户密码结合到这个方法里面来，提高代码复用性）
    @Transactional
    public boolean updateAccountPassword(String accountNumber, String oldPassword, String newPassword) {
        /*BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber); // 使用正确的方法名
        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        // 验证旧密码
        if (!account.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Invalid old password");
        }*/
        verifyAccountPassword(accountNumber, oldPassword);

        // 更新密码
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        account.setPassword(newPassword);
        int result = bankAccountMapper.updateAccount(account); // 使用正确的方法名
        return result > 0;
    }

    // 定期转活期
    /*@Transactional
    public boolean fixedToCurrent(String accountNumber, String password) {
        // 验证账户存在且有效
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        // 验证账户类型为定期
        if (!"FIXED".equals(account.getAccountType())) {
            throw new RuntimeException("Account is not a fixed deposit account");
        }

        // 验证密码
        if (!account.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        // 计算利息 - 这里简化处理，实际应根据存期和利率计算
        BigDecimal interest = calculateInterest(account);

        // 计算总额（本金+利息）
        BigDecimal totalAmount = account.getBalance().add(interest);

        // 更新账户类型为活期
        account.setAccountType("CURRENT");
        // 重置定期相关属性
        account.setTerm(null);
        account.setInterestRate(null);
        // 更新余额（包含利息）
        account.setBalance(totalAmount);

        bankAccountMapper.updateAccount(account);

        // 创建交易记录
        Transaction transacton = new Transaction(
                generateTransactionId(),
                accountNumber,
                null,
                totalAmount,
                "FIXED_TO_CURRENT",
                LocalDateTime.now(),
                "Convert fixed deposit to current account with interest: " + interest,
                "COMPLETED"
        );
        transactionMapper.insertTransaction(transaction);

        return transaction;
    }

    // 活期转定期
    @Transactional
    public Transaction currentToFixed(String accountNumber, String password, int termMonths, BigDecimal interestRate) {
        // 验证账户存在且有效
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        // 验证账户类型为活期
        if (!"CURRENT".equals(account.getAccountType())) {
            throw new RuntimeException("Account is not a current account");
        }

        // 验证密码
        if (!account.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        // 验证余额是否充足
        if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Insufficient balance for fixed deposit");
        }

        // 验证存期和利率是否有效
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Term must be greater than zero");
        }
        if (interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interest rate must be greater than zero");
        }

        // 更新账户类型为定期
        account.setAccountType("FIXED");
        // 设置定期相关属性
        account.setTerm(termMonths);  // 存期（月）
        account.setInterestRate(interestRate);  // 利率
        account.setFixedStartDate(LocalDateTime.now());  // 定期开始日期

        bankAccountMapper.updateAccount(account);

        // 创建交易记录
        Transaction transaction = new Transaction(
                generateTransactionId(),
                accountNumber,
                null,
                account.getBalance(),
                "CURRENT_TO_FIXED",
                LocalDateTime.now(),
                "Convert current account to fixed deposit, term: " + termMonths + " months, rate: " + interestRate,
                "COMPLETED"
        );
        transactionMapper.insertTransaction(transaction);

        return transaction;
    }

    // 计算定期利息（简化版）
    private BigDecimal calculateInterest(BankAccount account) {
        if (account.getTerm() == null || account.getInterestRate() == null ||
                account.getFixedStartDate() == null) {
            return BigDecimal.ZERO;
        }

        // 计算存期（月）
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
                account.getFixedStartDate(), LocalDateTime.now());

        // 实际存期不能超过约定存期
        months = Math.min(months, account.getTerm());

        // 利息 = 本金 × 利率 × 存期/12
        return account.getBalance()
                .multiply(account.getInterestRate())
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
    }*/

    // 活期转定期




}
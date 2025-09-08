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
import java.util.ArrayList;
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



    // 查询账户的所有定期存款记录
    public List<Transaction> getFixedDepositTransactions(String accountNumber) {
        // 验证账户存在
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        // 查询该账户的所有交易记录
        List<Transaction> allTransactions = transactionMapper.selectByAccountNumber(accountNumber);

        // 筛选出定期存款交易记录
        List<Transaction> fixedTransactions = new ArrayList<>();

        for (Transaction transaction : allTransactions) {
            String transactionType = transaction.getTransactionType();

            // 筛选定期存款交易记录
            if (transactionType != null &&
                    (transactionType.equals("CurrentToFixed1年") ||
                            transactionType.equals("CurrentToFixed3年") ||
                            transactionType.equals("CurrentToFixed5年"))) {

                fixedTransactions.add(transaction);
            }
        }

        return fixedTransactions;
    }



    // 根据定期类型计算利息（简化版）
    private BigDecimal calculateInterestByType(BigDecimal principal, String type) {
        // 简化处理：根据类型设定利率
        BigDecimal interestRate = switch (type) {
            case "1年" -> new BigDecimal("0.0175"); // 2.5%年利率
            case "3年" -> new BigDecimal("0.0275"); // 3.5%年利率
            case "5年" -> new BigDecimal("0.0325"); // 4.5%年利率
            default -> BigDecimal.ZERO;
        };

        // 简化计算：本金 × 利率
        return principal.multiply(interestRate);
    }

    //定期转活期操作
    @Transactional
    public boolean convertFixedToCurrent(String accountNumber, String transactionId, String password) {
        // 验证账户存在且有效
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("账户状态异常");
        }

        // 验证密码
        if (!account.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // 查找对应的定期存款交易记录
        Transaction fixedTransaction = transactionMapper.selectByTransactionId(transactionId);
        if (fixedTransaction == null) {
            throw new RuntimeException("未找到对应的定期存款记录");
        }

        // 验证交易记录是否属于该账户
        if (!accountNumber.equals(fixedTransaction.getFromAccountNumber())) {
            throw new RuntimeException("交易记录与账户不匹配");
        }

        // 验证交易类型是否为定期存款
        String transactionType = fixedTransaction.getTransactionType();
        if (!transactionType.startsWith("CurrentToFixed")) {
            throw new RuntimeException("该交易记录不是定期存款");
        }

        // 检查是否已经到期
        if (!isDepositMatured(fixedTransaction.getTransactionTime(), transactionType)) {
            throw new RuntimeException("定期存款尚未到期");
        }

        // 计算本息和
        BigDecimal principal = fixedTransaction.getAmount();
        BigDecimal interest = calculateInterestByType(principal, extractDepositType(transactionType));
        BigDecimal totalAmount = principal.add(interest);

        // 更新账户余额（将本息和加入活期余额）
        BigDecimal newBalance = account.getBalance().add(totalAmount);
        bankAccountMapper.updateBalance(accountNumber, newBalance);

        // 创建新的交易记录（定期转活期）
        Transaction transaction = new Transaction(
                generateTransactionId(),
                accountNumber,
                accountNumber,
                totalAmount,
                "FixedToCurrent" + extractDepositType(transactionType),
                LocalDateTime.now(),
                "定期转活期，本金: " + principal + "，利息: " + interest,
                "COMPLETED"
        );
        transactionMapper.insertTransaction(transaction);

        return true;
    }

    // 从交易类型中提取存款类型（如"1年"）
    private String extractDepositType(String transactionType) {
        return transactionType.replace("CurrentToFixed", "");
    }

    // 判断定期存款是否到期
    private boolean isDepositMatured(LocalDateTime depositTime, String transactionType) {
        if (depositTime == null || transactionType == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maturityDate = null;

        String depositType = extractDepositType(transactionType);

        maturityDate = switch (depositType) {
            case "1年" -> depositTime.plusYears(1);
            case "3年" -> depositTime.plusYears(3);
            case "5年" -> depositTime.plusYears(5);
            default -> maturityDate;
        };

        return maturityDate != null && !now.isBefore(maturityDate);
    }




    // 活期转定期
    @Transactional
    public Transaction convertCurrentToFixed(String accountNumber, BigDecimal amount, String password,String type) {
        // 验证账户存在且有效
        BankAccount account = bankAccountMapper.selectByAccountNumber(accountNumber);
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
            throw new RuntimeException("余额不足！");
        }

        // 更新余额
        BigDecimal newBalance = account.getBalance().subtract(amount);
        bankAccountMapper.updateBalance(accountNumber, newBalance);

        // 创建交易记录
        Transaction transaction = new Transaction(
                generateTransactionId(),
                accountNumber,
                accountNumber,
                amount,
                "CurrentToFixed"+type,
                LocalDateTime.now(),
                "活期转定期"+amount,
                "COMPLETED"
        );
        transactionMapper.insertTransaction(transaction);

        return transaction;
    }


    // 处理时间解读的方法方法
    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().withNano(0); // 去除纳秒部分
    }






}
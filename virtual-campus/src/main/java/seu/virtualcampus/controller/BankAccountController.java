// BankAccountController.java
package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.BankAccount;
import seu.virtualcampus.domain.Transaction;
import seu.virtualcampus.service.BankAccountService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 银行账户控制器。
 * <p>
 * 提供与银行账户相关的API接口，包括开户、存款、取款、转账、查询、状态更新等功能。
 * 同时包含了管理员对账户和交易的管理功能，以及特殊的消费功能。
 */
@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    private static final Logger logger = Logger.getLogger(BankAccountController.class.getName());
    @Autowired
    private BankAccountService bankAccountService;

    /**
     * 开设一个新的银行账户。
     *
     * @param userId 用户ID。
     * @param accountType 账户类型。
     * @param password 账户密码。
     * @param initialDeposit 初始存款金额。
     * @return 创建的银行账户信息。
     */
    @PostMapping("/open")
    public ResponseEntity<BankAccount> openAccount(
            @RequestParam String userId,
            @RequestParam String accountType,
            @RequestParam String password,
            @RequestParam BigDecimal initialDeposit) {
        BankAccount account = bankAccountService.createAccount(userId, accountType, password, initialDeposit);
        return ResponseEntity.ok(account);
    }

    /**
     * 向指定账户存款。
     *
     * @param accountNumber 账户号码。
     * @param amount 存款金额。
     * @return 本次存款的交易记录。
     */
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<Transaction> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        Transaction transaction = bankAccountService.processDeposit(accountNumber, amount);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 从指定账户取款。
     *
     * @param accountNumber 账户号码。
     * @param amount 取款金额。
     * @param password 账户密码。
     * @return 本次取款的交易记录。
     */
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam String password) {
        Transaction transaction = bankAccountService.processWithdrawal(accountNumber, amount, password);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 从一个账户向另一个账户转账。
     *
     * @param fromAccount 转出账户的号码。
     * @param toAccount 转入账户的号码。
     * @param amount 转账金额。
     * @param password 转出账户的密码。
     * @return 本次转账的交易记录。
     */
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount,
            @RequestParam String password) {
        Transaction transaction = bankAccountService.processTransfer(fromAccount, toAccount, amount, password);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 查询指定账户的详细信息。
     *
     * @param accountNumber 账户号码。
     * @return 银行账户的详细信息。
     */
    @GetMapping("/{accountNumber}/accountInfo")
    public ResponseEntity<BankAccount> getAccountInfo(@PathVariable String accountNumber) {
        BankAccount account = bankAccountService.getAccountInfo(accountNumber);
        return ResponseEntity.ok(account);
    }

    /**
     * 查询指定时间范围内的交易记录。
     * <p>
     * 如果账户号码为 "all"，则查询所有账户在该时间范围内的交易记录。
     *
     * @param accountNumber 账户号码，或 "all" 表示所有账户。
     * @param start 查询的开始时间。
     * @param end 查询的结束时间。
     * @return 符合条件的交易记录列表。
     */
    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Transaction> transactions;

        // 如果accountNumber是"all"，则查询所有交易记录
        if ("all".equals(accountNumber)) {
            transactions = bankAccountService.getAllTransactionsByTimeRange(start, end);
        } else {
            transactions = bankAccountService.getTransactionHistory(accountNumber, start, end);
        }

        return ResponseEntity.ok(transactions);
    }

    /**
     * 更新账户的状态。
     *
     * @param accountNumber 账户号码。
     * @param newStatus 新的账户状态。
     * @return 操作是否成功。
     */
    @PutMapping("/{accountNumber}/status")
    public ResponseEntity<Boolean> updateStatus(
            @PathVariable String accountNumber,
            @RequestParam String newStatus) {
        boolean result = bankAccountService.updateAccountStatus(accountNumber, newStatus);
        return ResponseEntity.ok(result);
    }

    /**
     * 验证账户密码是否正确。
     *
     * @param accountNumber 账户号码。
     * @param password 待验证的密码。
     * @return 密码是否有效。
     */
    @PostMapping("/{accountNumber}/verify-password")
    public ResponseEntity<Boolean> verifyPassword(
            @PathVariable String accountNumber,
            @RequestParam String password) {
        boolean isValid = bankAccountService.verifyAccountPassword(accountNumber, password);
        return ResponseEntity.ok(isValid);
    }

    /**
     * 验证管理员账户的密码。
     *
     * @param accountNumber 管理员账户号码。
     * @param password 待验证的密码。
     * @return 如果密码有效，返回true；否则返回false或错误请求。
     */
    @PostMapping("/{accountNumber}/verify-admin-password")
    public ResponseEntity<Boolean> verifyAdminPassword(
            @PathVariable String accountNumber,
            @RequestParam String password) {
        try {
            boolean isValid = bankAccountService.verifyAdminPassword(accountNumber, password);
            return ResponseEntity.ok(isValid);
        } catch (RuntimeException e) {
            // 账户不存在或其他运行时异常，记录错误日志并返回false
            logger.severe("验证管理员密码时发生异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }


    /**
     * 更新账户密码。
     *
     * @param accountNumber 账户号码。
     * @param oldPassword 旧密码。
     * @param newPassword 新密码。
     * @return 操作是否成功。
     */
    @PutMapping("/{accountNumber}/password")
    public ResponseEntity<Boolean> updatePassword(
            @PathVariable String accountNumber,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        boolean result = bankAccountService.updateAccountPassword(accountNumber, oldPassword, newPassword);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取指定账户的所有定期存款记录。
     *
     * @param accountNumber 账户号码。
     * @return 定期存款交易记录的列表。
     */
    @GetMapping("/{accountNumber}/fixed-deposits")
    public ResponseEntity<List<Transaction>> getFixedDepositRecords(
            @PathVariable String accountNumber) {
        List<Transaction> fixedDepositTransactions = bankAccountService.getFixedDepositTransactions(accountNumber);
        return ResponseEntity.ok(fixedDepositTransactions);
    }

    /**
     * 将一笔定期存款转为活期存款。
     *
     * @param accountNumber 账户号码。
     * @param request 请求体，包含 "transactionId" (定期存款的交易ID) 和 "password" (账户密码)。
     * @return 操作成功或失败的消息。
     */
    @PostMapping("/{accountNumber}/fixed-to-current")
    public ResponseEntity<String> convertFixedToCurrent(
            @PathVariable String accountNumber,
            @RequestBody Map<String, String> request) {
        try {
            String transactionId = request.get("transactionId");
            String password = request.get("password");

            boolean result = bankAccountService.convertFixedToCurrent(accountNumber, transactionId, password);

            if (result) {
                return ResponseEntity.ok("定期转活期操作成功");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("定期转活期操作失败");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * 将活期存款转为定期存款。
     *
     * @param accountNumber 账户号码。
     * @param request 请求体，包含 "amount" (金额), "password" (密码), 和 "term" (存款期限)。
     * @return 包含操作结果和新交易信息的响应。
     */
    @PostMapping("/{accountNumber}/current-to-fixed")
    public ResponseEntity<Map<String, Object>> convertCurrentToFixed(
            @PathVariable String accountNumber,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String amountStr = request.get("amount");
            String password = request.get("password");
            String term = request.get("term");

            BigDecimal amount = new BigDecimal(amountStr);

            Transaction transaction = bankAccountService.convertCurrentToFixed(accountNumber, amount, password, term);

            response.put("success", true);
            response.put("message", "活期转定期操作成功");
            response.put("data", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    //管理员部分***********************************************************

    /**
     * (管理员) 获取所有银行账户信息。
     *
     * @return 所有银行账户的列表。
     */
    @GetMapping("/all-accounts")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * (管理员) 获取所有交易记录。
     *
     * @return 所有交易记录的列表。
     */
    @GetMapping("/all-transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = bankAccountService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * (管理员) 根据交易ID获取交易记录。
     *
     * @param transactionId 交易ID。
     * @return 对应的交易记录。
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String transactionId) {
        Transaction transaction = bankAccountService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    /**
     * (管理员) 更新账户类型，可用于设置管理员权限。
     *
     * @param accountNumber 账户号码。
     * @param newAccountType 新的账户类型。
     * @return 操作是否成功。
     */
    @PutMapping("/{accountNumber}/account-type")
    public ResponseEntity<Boolean> updateAccountType(
            @PathVariable String accountNumber,
            @RequestParam String newAccountType) {
        try {
            boolean result = bankAccountService.updateAccountType(accountNumber, newAccountType);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }


    // 商店消费*********************************************

    /**
     * 商店消费接口
     *
     * @param fromAccount 消费者账户
     * @param password    消费者账户密码
     * @param toAccount   商家账户
     * @param amount      消费金额
     * @return 交易记录
     */
    @PostMapping("/shopping")
    public ResponseEntity<Transaction> shopping(
            @RequestParam String fromAccount,
            @RequestParam String password,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount) {
        Transaction transaction = bankAccountService.processShopping(fromAccount, password, toAccount, amount);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 信用支付（花呗）接口。
     *
     * @param fromAccount 消费者账户。
     * @param password 消费者账户密码。
     * @param toAccount 商家账户。
     * @param amount 消费金额。
     * @return 交易记录。
     */
    @PostMapping("/paylater")
    public ResponseEntity<Transaction> payLater(
            @RequestParam String fromAccount,
            @RequestParam String password,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount) {
        Transaction transaction = bankAccountService.processPayLater(fromAccount, password, toAccount, amount);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 检查并更新所有已违约的信用支付交易的状态。
     *
     * @return 更新状态的交易数量。
     */
    @GetMapping("/check-overdue-transactions")
    public ResponseEntity<Integer> checkOverdueTransactions() {
        int updatedCount = bankAccountService.checkAndMarkOverdueTransactions();
        return ResponseEntity.ok(updatedCount);
    }

}
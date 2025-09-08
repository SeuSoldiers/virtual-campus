// BankAccountController.java
package seu.virtualcampus.controller;

import org.springframework.http.HttpStatus;
import seu.virtualcampus.domain.BankAccount;
import seu.virtualcampus.domain.Transaction;
import seu.virtualcampus.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    // 开户业务
    @PostMapping("/open")
    public ResponseEntity<BankAccount> openAccount(
            @RequestParam String userId,
            @RequestParam String accountType,
            @RequestParam String password,
            @RequestParam BigDecimal initialDeposit) {
        BankAccount account = bankAccountService.createAccount(userId, accountType, password,initialDeposit);
        return ResponseEntity.ok(account);
    }

    // 存款业务
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<Transaction> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        Transaction transaction = bankAccountService.processDeposit(accountNumber, amount);
        return ResponseEntity.ok(transaction);
    }

    // 取款业务
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam String password) {
        Transaction transaction = bankAccountService.processWithdrawal(accountNumber, amount, password);
        return ResponseEntity.ok(transaction);
    }

    // 转账业务
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount,
            @RequestParam String password) {
        Transaction transaction = bankAccountService.processTransfer(fromAccount, toAccount, amount, password);
        return ResponseEntity.ok(transaction);
    }

    // 余额查询（***更改为个人信息查询）
    @GetMapping("/{accountNumber}/accountInfo")
    public ResponseEntity<BankAccount> getAccountInfo(@PathVariable String accountNumber) {
        BankAccount account = bankAccountService.getAccountInfo(accountNumber);
        return ResponseEntity.ok(account);
    }

    // 交易记录查询
    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> transactions = bankAccountService.getTransactionHistory(accountNumber, start, end);
        return ResponseEntity.ok(transactions);
    }

    // 更新账户状态
    @PutMapping("/{accountNumber}/status")
    public ResponseEntity<Boolean> updateStatus(
            @PathVariable String accountNumber,
            @RequestParam String newStatus) {
        boolean result = bankAccountService.updateAccountStatus(accountNumber, newStatus);
        return ResponseEntity.ok(result);
    }

    // 新增：验证账户密码
    @PostMapping("/{accountNumber}/verify-password")
    public ResponseEntity<Boolean> verifyPassword(
            @PathVariable String accountNumber,
            @RequestParam String password) {
        boolean isValid = bankAccountService.verifyAccountPassword(accountNumber, password);
        return ResponseEntity.ok(isValid);
    }

    // 新增：更新账户密码
    @PutMapping("/{accountNumber}/password")
    public ResponseEntity<Boolean> updatePassword(
            @PathVariable String accountNumber,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        boolean result = bankAccountService.updateAccountPassword(accountNumber, oldPassword, newPassword);
        return ResponseEntity.ok(result);
    }

    // 获取定期存款记录
    @GetMapping("/{accountNumber}/fixed-deposits")
    public ResponseEntity<List<Transaction>> getFixedDepositRecords(
            @PathVariable String accountNumber) {
        List<Transaction> fixedDepositTransactions = bankAccountService.getFixedDepositTransactions(accountNumber);
        return ResponseEntity.ok(fixedDepositTransactions);
    }

    //定期转活期
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



    // 活期转定期
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


}
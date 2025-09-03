// BankAccountController.java
package seu.virtualcampus.controller;

import seu.virtualcampus.domain.BankAccount;
import seu.virtualcampus.domain.Transaction;
import seu.virtualcampus.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    // *定期转活期

    // *活期转定期


}
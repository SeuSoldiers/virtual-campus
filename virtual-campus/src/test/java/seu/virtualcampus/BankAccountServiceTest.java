// BankAccountServiceTest.java
package seu.virtualcampus.service;

import seu.virtualcampus.domain.BankAccount;
import seu.virtualcampus.domain.Transaction;
import seu.virtualcampus.mapper.BankAccountMapper;
import seu.virtualcampus.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceTest {

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private BankAccountService bankAccountService;

    private BankAccount testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testAccount = new BankAccount();
        testAccount.setAccountNumber("AC123456789");
        testAccount.setUserId("USER001");
        testAccount.setAccountType("SAVINGS");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus("ACTIVE");
        testAccount.setCreatedDate(LocalDateTime.now());

        testTransaction = new Transaction();
        testTransaction.setTransactionId("TX987654321");
        testTransaction.setFromAccountNumber(null);
        testTransaction.setToAccountNumber("AC123456789");
        testTransaction.setAmount(new BigDecimal("500.00"));
        testTransaction.setTransactionType("DEPOSIT");
        testTransaction.setTransactionTime(LocalDateTime.now());
        testTransaction.setRemark("Test deposit");
        testTransaction.setStatus("COMPLETED");
    }

    @Test
    void testCreateAccount() {
        // 准备
        when(bankAccountMapper.insertAccount(any(BankAccount.class))).thenReturn(1);
        when(transactionMapper.insertTransaction(any(Transaction.class))).thenReturn(1);

        // 执行
        BankAccount result = bankAccountService.createAccount("USER001", "SAVINGS", new BigDecimal("500.00"));

        // 验证
        assertNotNull(result);
        assertNotNull(result.getAccountNumber());
        assertEquals("USER001", result.getUserId());
        assertEquals("SAVINGS", result.getAccountType());
        assertEquals(new BigDecimal("500.00"), result.getBalance());
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getCreatedDate());

        // 验证方法调用
        verify(bankAccountMapper, times(1)).insertAccount(any(BankAccount.class));
        verify(transactionMapper, times(1)).insertTransaction(any(Transaction.class));
    }

    @Test
    void testProcessDeposit() {
        // 准备
        when(bankAccountMapper.selectByAccountNumber("AC123456789")).thenReturn(testAccount);
        when(bankAccountMapper.updateBalance(anyString(), any(BigDecimal.class))).thenReturn(1);
        when(transactionMapper.insertTransaction(any(Transaction.class))).thenReturn(1);

        // 执行
        Transaction result = bankAccountService.processDeposit("AC123456789", new BigDecimal("500.00"));

        // 验证
        assertNotNull(result);
        assertEquals("AC123456789", result.getToAccountNumber());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        assertEquals("DEPOSIT", result.getTransactionType());
        assertEquals("COMPLETED", result.getStatus());

        // 验证方法调用
        verify(bankAccountMapper, times(1)).selectByAccountNumber("AC123456789");
        verify(bankAccountMapper, times(1)).updateBalance(anyString(), any(BigDecimal.class));
        verify(transactionMapper, times(1)).insertTransaction(any(Transaction.class));
    }

    @Test
    void testProcessDepositAccountNotFound() {
        // 准备
        when(bankAccountMapper.selectByAccountNumber("AC999999999")).thenReturn(null);

        // 执行和验证
        assertThrows(RuntimeException.class, () -> {
            bankAccountService.processDeposit("AC999999999", new BigDecimal("500.00"));
        });

        // 验证方法调用
        verify(bankAccountMapper, times(1)).selectByAccountNumber("AC999999999");
        verify(bankAccountMapper, never()).updateBalance(anyString(), any(BigDecimal.class));
        verify(transactionMapper, never()).insertTransaction(any(Transaction.class));
    }

    @Test
    void testProcessWithdrawal() {
        // 准备
        when(bankAccountMapper.selectByAccountNumber("AC123456789")).thenReturn(testAccount);
        when(bankAccountMapper.updateBalance(anyString(), any(BigDecimal.class))).thenReturn(1);
        when(transactionMapper.insertTransaction(any(Transaction.class))).thenReturn(1);

        // 执行
        Transaction result = bankAccountService.processWithdrawal("AC123456789", new BigDecimal("200.00"), "password123");

        // 验证
        assertNotNull(result);
        assertEquals("AC123456789", result.getFromAccountNumber());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
        assertEquals("WITHDRAWAL", result.getTransactionType());
        assertEquals("COMPLETED", result.getStatus());

        // 验证方法调用
        verify(bankAccountMapper, times(1)).selectByAccountNumber("AC123456789");
        verify(bankAccountMapper, times(1)).updateBalance(anyString(), any(BigDecimal.class));
        verify(transactionMapper, times(1)).insertTransaction(any(Transaction.class));
    }

    @Test
    void testProcessWithdrawalInsufficientBalance() {
        // 准备
        when(bankAccountMapper.selectByAccountNumber("AC123456789")).thenReturn(testAccount);

        // 执行和验证
        assertThrows(RuntimeException.class, () -> {
            bankAccountService.processWithdrawal("AC123456789", new BigDecimal("2000.00"), "password123");
        });

        // 验证方法调用
        verify(bankAccountMapper, times(1)).selectByAccountNumber("AC123456789");
        verify(bankAccountMapper, never()).updateBalance(anyString(), any(BigDecimal.class));
        verify(transactionMapper, never()).insertTransaction(any(Transaction.class));
    }

    @Test
    void testProcessTransfer() {
        // 准备
        BankAccount fromAccount = new BankAccount();
        fromAccount.setAccountNumber("AC111111111");
        fromAccount.setUserId("USER001");
        fromAccount.setAccountType("SAVINGS");
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount.setStatus("ACTIVE");
        fromAccount.setCreatedDate(LocalDateTime.now());

        BankAccount toAccount = new BankAccount();
        toAccount.setAccountNumber("AC222222222");
        toAccount.setUserId("USER002");
        toAccount.setAccountType("SAVINGS");
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setStatus("ACTIVE");
        toAccount.setCreatedDate(LocalDateTime.now());

        when(bankAccountMapper.selectByAccountNumber("AC111111111")).thenReturn(fromAccount);
        when(bankAccountMapper.selectByAccountNumber("AC222222222")).thenReturn(toAccount);
        when(bankAccountMapper.updateBalance(anyString(), any(BigDecimal.class))).thenReturn(1);
        when(transactionMapper.insertTransaction(any(Transaction.class))).thenReturn(1);

        // 执行
        Transaction result = bankAccountService.processTransfer("AC111111111", "AC222222222", new BigDecimal("300.00"), "password123");

        // 验证
        assertNotNull(result);
        assertEquals("AC111111111", result.getFromAccountNumber());
        assertEquals("AC222222222", result.getToAccountNumber());
        assertEquals(new BigDecimal("300.00"), result.getAmount());
        assertEquals("TRANSFER", result.getTransactionType());
        assertEquals("COMPLETED", result.getStatus());

        // 验证方法调用
        verify(bankAccountMapper, times(1)).selectByAccountNumber("AC111111111");
        verify(bankAccountMapper, times(1)).selectByAccountNumber("AC222222222");
        verify(bankAccountMapper, times(2)).updateBalance(anyString(), any(BigDecimal.class));
        verify(transactionMapper, times(1)).insertTransaction(any(Transaction.class));
    }

    @Test
    void testGetAccountBalance() {
        // 准备
        when(bankAccountMapper.selectByAccountNumber("AC123456789")).thenReturn(testAccount);

        // 执行
        BigDecimal result = bankAccountService.getAccountBalance("AC123456789");

        // 验证
        assertEquals(new BigDecimal("1000.00"), result);

        // 验证方法调用
        verify(bankAccountMapper, times(1)).selectByAccountNumber("AC123456789");
    }

    @Test
    void testGetTransactionHistory() {
        // 准备
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionMapper.selectByAccountNumberAndTimeRange("AC123456789", start, end)).thenReturn(transactions);

        // 执行
        List<Transaction> result = bankAccountService.getTransactionHistory("AC123456789", start, end);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TX987654321", result.get(0).getTransactionId());

        // 验证方法调用
        verify(transactionMapper, times(1)).selectByAccountNumberAndTimeRange("AC123456789", start, end);
    }

    @Test
    void testUpdateAccountStatus() {
        // 准备
        when(bankAccountMapper.updateStatus("AC123456789", "FROZEN")).thenReturn(1);

        // 执行
        boolean result = bankAccountService.updateAccountStatus("AC123456789", "FROZEN");

        // 验证
        assertTrue(result);

        // 验证方法调用
        verify(bankAccountMapper, times(1)).updateStatus("AC123456789", "FROZEN");
    }
}
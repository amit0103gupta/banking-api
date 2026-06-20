package com.xbank.banking_api.service;

import com.xbank.banking_api.model.Account;
import com.xbank.banking_api.model.Transaction;
import com.xbank.banking_api.repository.AccountRepository;
import com.xbank.banking_api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setAccountNumber("SAN111111111");
        fromAccount.setAccountType(Account.AccountType.SAVINGS);
        fromAccount.setBalance(BigDecimal.valueOf(5000));
        fromAccount.setStatus(Account.AccountStatus.ACTIVE);

        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber("SAN222222222");
        toAccount.setAccountType(Account.AccountType.CURRENT);
        toAccount.setBalance(BigDecimal.valueOf(2000));
        toAccount.setStatus(Account.AccountStatus.ACTIVE);
    }

    // --- Deposit tests ---

    @Test
    void deposit_success() {
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        Transaction result = transactionService.deposit("SAN111111111",
                BigDecimal.valueOf(500), "Salary");

        assertNotNull(result);
        assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        assertEquals("Salary", result.getDescription());
        assertEquals(BigDecimal.valueOf(5500), fromAccount.getBalance());
        verify(accountRepository).save(fromAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deposit_nullAmount_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit("SAN111111111", null, "Deposit")
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Amount must be greater than zero"));
    }

    @Test
    void deposit_zeroAmount_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit("SAN111111111", BigDecimal.ZERO, "Deposit")
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void deposit_negativeAmount_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit("SAN111111111", BigDecimal.valueOf(-100), "Deposit")
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void deposit_accountNotFound_throwsNotFound() {
        when(accountRepository.findByAccountNumber("SAN000000000"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit("SAN000000000",
                        BigDecimal.valueOf(100), "Deposit")
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void deposit_inactiveAccount_throwsBadRequest() {
        fromAccount.setStatus(Account.AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit("SAN111111111",
                        BigDecimal.valueOf(100), "Deposit")
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Account is not active"));
    }

    @Test
    void deposit_closedAccount_throwsBadRequest() {
        fromAccount.setStatus(Account.AccountStatus.CLOSED);
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit("SAN111111111",
                        BigDecimal.valueOf(100), "Deposit")
        );
    }

    // --- Withdraw tests ---

    @Test
    void withdraw_success() {
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        Transaction result = transactionService.withdraw("SAN111111111",
                BigDecimal.valueOf(1000), "ATM withdrawal");

        assertNotNull(result);
        assertEquals(Transaction.TransactionType.WITHDRAWAL, result.getType());
        assertEquals(BigDecimal.valueOf(1000), result.getAmount());
        assertEquals(BigDecimal.valueOf(4000), fromAccount.getBalance());
    }

    @Test
    void withdraw_insufficientBalance_throwsBadRequest() {
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.withdraw("SAN111111111",
                        BigDecimal.valueOf(10000), "Withdrawal")
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Insufficient balance"));
    }

    @Test
    void withdraw_exactBalance_success() {
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        Transaction result = transactionService.withdraw("SAN111111111",
                BigDecimal.valueOf(5000), "Full withdrawal");

        assertEquals(BigDecimal.ZERO, fromAccount.getBalance().stripTrailingZeros());
    }

    @Test
    void withdraw_nullAmount_throwsBadRequest() {
        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.withdraw("SAN111111111", null, "Withdrawal")
        );
    }

    @Test
    void withdraw_accountNotFound_throwsNotFound() {
        when(accountRepository.findByAccountNumber("SAN000000000"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.withdraw("SAN000000000",
                        BigDecimal.valueOf(100), "Withdrawal")
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void withdraw_inactiveAccount_throwsBadRequest() {
        fromAccount.setStatus(Account.AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.withdraw("SAN111111111",
                        BigDecimal.valueOf(100), "Withdrawal")
        );
    }

    // --- Transfer tests ---

    @Test
    void transfer_success() {
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("SAN222222222"))
                .thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        Transaction result = transactionService.transfer("SAN111111111",
                "SAN222222222", BigDecimal.valueOf(1000), "Rent payment");

        assertNotNull(result);
        assertEquals(Transaction.TransactionType.TRANSFER, result.getType());
        assertEquals(BigDecimal.valueOf(1000), result.getAmount());
        assertEquals(BigDecimal.valueOf(4000), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(3000), toAccount.getBalance());
        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
    }

    @Test
    void transfer_sameAccount_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.transfer("SAN111111111",
                        "SAN111111111", BigDecimal.valueOf(100), "Self transfer")
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Cannot transfer to the same account"));
    }

    @Test
    void transfer_insufficientBalance_throwsBadRequest() {
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("SAN222222222"))
                .thenReturn(Optional.of(toAccount));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.transfer("SAN111111111",
                        "SAN222222222", BigDecimal.valueOf(10000), "Big transfer")
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Insufficient balance"));
    }

    @Test
    void transfer_fromAccountNotFound_throwsNotFound() {
        when(accountRepository.findByAccountNumber("SAN000000000"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.transfer("SAN000000000",
                        "SAN222222222", BigDecimal.valueOf(100), "Transfer")
        );
    }

    @Test
    void transfer_toAccountNotFound_throwsNotFound() {
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("SAN000000000"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.transfer("SAN111111111",
                        "SAN000000000", BigDecimal.valueOf(100), "Transfer")
        );
    }

    @Test
    void transfer_nullAmount_throwsBadRequest() {
        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.transfer("SAN111111111",
                        "SAN222222222", null, "Transfer")
        );
    }

    @Test
    void transfer_inactiveFromAccount_throwsBadRequest() {
        fromAccount.setStatus(Account.AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.transfer("SAN111111111",
                        "SAN222222222", BigDecimal.valueOf(100), "Transfer")
        );
    }

    @Test
    void transfer_inactiveToAccount_throwsBadRequest() {
        toAccount.setStatus(Account.AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber("SAN111111111"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("SAN222222222"))
                .thenReturn(Optional.of(toAccount));

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.transfer("SAN111111111",
                        "SAN222222222", BigDecimal.valueOf(100), "Transfer")
        );
    }

    // --- Transaction history tests ---

    @Test
    void getTransactionHistory_returnsList() {
        Transaction tx1 = new Transaction();
        tx1.setId(1L);
        tx1.setType(Transaction.TransactionType.DEPOSIT);
        tx1.setAmount(BigDecimal.valueOf(500));

        Transaction tx2 = new Transaction();
        tx2.setId(2L);
        tx2.setType(Transaction.TransactionType.WITHDRAWAL);
        tx2.setAmount(BigDecimal.valueOf(200));

        when(transactionRepository.findAllByAccountNumber("SAN111111111"))
                .thenReturn(Arrays.asList(tx1, tx2));

        List<Transaction> result = transactionService.getTransactionHistory("SAN111111111");

        assertEquals(2, result.size());
        verify(transactionRepository).findAllByAccountNumber("SAN111111111");
    }

    @Test
    void getTransactionHistory_emptyList() {
        when(transactionRepository.findAllByAccountNumber("SAN111111111"))
                .thenReturn(List.of());

        List<Transaction> result = transactionService.getTransactionHistory("SAN111111111");

        assertTrue(result.isEmpty());
    }
}

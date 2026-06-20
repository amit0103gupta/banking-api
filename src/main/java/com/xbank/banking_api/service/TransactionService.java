package com.xbank.banking_api.service;

import com.xbank.banking_api.exception.InsufficientBalanceException;
import com.xbank.banking_api.exception.InvalidOperationException;
import com.xbank.banking_api.exception.ResourceNotFoundException;
import com.xbank.banking_api.model.Account;
import com.xbank.banking_api.model.Transaction;
import com.xbank.banking_api.repository.AccountRepository;
import com.xbank.banking_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = getActiveAccount(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(Transaction.TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setToAccount(account);
        tx.setDescription(description);
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = getActiveAccount(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                "Insufficient balance in account: " + accountNumber);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(Transaction.TransactionType.WITHDRAWAL);
        tx.setAmount(amount);
        tx.setFromAccount(account);
        tx.setDescription(description);
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction transfer(String fromAccountNumber, String toAccountNumber,
                                BigDecimal amount, String description) {
        validateAmount(amount);

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidOperationException(
                "Cannot transfer to the same account: " + fromAccountNumber);
        }

        Account fromAccount = getActiveAccount(fromAccountNumber);
        Account toAccount   = getActiveAccount(toAccountNumber);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                "Insufficient balance in account: " + fromAccountNumber);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction tx = new Transaction();
        tx.setType(Transaction.TransactionType.TRANSFER);
        tx.setAmount(amount);
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setDescription(description);
        return transactionRepository.save(tx);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new ResourceNotFoundException(
                    "Account not found: " + accountNumber);
        }
        return transactionRepository.findAllByAccountNumber(accountNumber);
    }

    private Account getActiveAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountNumber));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidOperationException(
                "Account is not active: " + accountNumber);
        }
        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(
                "Amount must be greater than zero");
        }
    }
}

package com.xbank.banking_api.service;

import com.xbank.banking_api.model.Account;
import com.xbank.banking_api.model.Transaction;
import com.xbank.banking_api.repository.AccountRepository;
import com.xbank.banking_api.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Insufficient balance");
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
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Cannot transfer to the same account");
        }

        Account fromAccount = getActiveAccount(fromAccountNumber);
        Account toAccount   = getActiveAccount(toAccountNumber);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Insufficient balance");
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
        return transactionRepository.findAllByAccountNumber(accountNumber);
    }

    private Account getActiveAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: " + accountNumber));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Account is not active: " + accountNumber);
        }
        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
    }
}

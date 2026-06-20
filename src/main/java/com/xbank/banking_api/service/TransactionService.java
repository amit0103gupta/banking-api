package com.xbank.banking_api.service;

import com.xbank.banking_api.exception.BusinessValidationException;
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
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository,
                               AccountRepository accountRepository,
                               AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = getActiveAccount(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        return createTransaction(Transaction.TransactionType.DEPOSIT, amount,
                null, account, description);
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = getActiveAccount(accountNumber);
        ensureSufficientBalance(account, amount);

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        return createTransaction(Transaction.TransactionType.WITHDRAWAL, amount,
                account, null, description);
    }

    @Transactional
    public Transaction transfer(String fromAccountNumber, String toAccountNumber,
                                BigDecimal amount, String description) {
        validateAmount(amount);

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new BusinessValidationException("Cannot transfer to the same account");
        }

        Account fromAccount = getActiveAccount(fromAccountNumber);
        Account toAccount   = getActiveAccount(toAccountNumber);
        ensureSufficientBalance(fromAccount, amount);

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return createTransaction(Transaction.TransactionType.TRANSFER, amount,
                fromAccount, toAccount, description);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactionRepository.findAllByAccountNumber(accountNumber);
    }

    private Account getActiveAccount(String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber);

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessValidationException(
                "Account is not active: " + accountNumber);
        }
        return account;
    }

    private Transaction createTransaction(Transaction.TransactionType type,
                                           BigDecimal amount,
                                           Account fromAccount,
                                           Account toAccount,
                                           String description) {
        Transaction tx = new Transaction();
        tx.setType(type);
        tx.setAmount(amount);
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setDescription(description);
        return transactionRepository.save(tx);
    }

    private void ensureSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessValidationException("Insufficient balance");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Amount must be greater than zero");
        }
    }
}

package com.xbank.banking_api.service;

import com.xbank.banking_api.exception.ResourceNotFoundException;
import com.xbank.banking_api.model.Account;
import com.xbank.banking_api.model.Customer;
import com.xbank.banking_api.repository.AccountRepository;
import com.xbank.banking_api.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account openAccount(Long customerId, Account.AccountType accountType) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountType(accountType);
        account.setAccountNumber(generateAccountNumber());

        return accountRepository.save(account);
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountNumber));
    }

    public List<Account> getAccountsByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }
        return accountRepository.findByCustomerId(customerId);
    }

    public BigDecimal getBalance(String accountNumber) {
        return getAccountByNumber(accountNumber).getBalance();
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "SAN" + (100000000 + new Random().nextInt(900000000));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}

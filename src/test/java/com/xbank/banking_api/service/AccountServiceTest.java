package com.xbank.banking_api.service;

import com.xbank.banking_api.model.Account;
import com.xbank.banking_api.model.Customer;
import com.xbank.banking_api.repository.AccountRepository;
import com.xbank.banking_api.repository.CustomerRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("SAN123456789");
        account.setAccountType(Account.AccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setCustomer(customer);
    }

    @Test
    void openAccount_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Account result = accountService.openAccount(1L, Account.AccountType.SAVINGS);

        assertNotNull(result);
        assertEquals(Account.AccountType.SAVINGS, result.getAccountType());
        assertEquals(customer, result.getCustomer());
        assertNotNull(result.getAccountNumber());
        assertTrue(result.getAccountNumber().startsWith("SAN"));
        verify(customerRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void openAccount_customerNotFound_throwsNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.openAccount(99L, Account.AccountType.SAVINGS)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Customer not found"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void openAccount_currentAccountType() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.openAccount(1L, Account.AccountType.CURRENT);

        assertEquals(Account.AccountType.CURRENT, result.getAccountType());
    }

    @Test
    void getAccountByNumber_found() {
        when(accountRepository.findByAccountNumber("SAN123456789"))
                .thenReturn(Optional.of(account));

        Account result = accountService.getAccountByNumber("SAN123456789");

        assertNotNull(result);
        assertEquals("SAN123456789", result.getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
    }

    @Test
    void getAccountByNumber_notFound_throwsNotFound() {
        when(accountRepository.findByAccountNumber("SAN000000000"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.getAccountByNumber("SAN000000000")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Account not found"));
    }

    @Test
    void getAccountsByCustomer_returnsList() {
        Account account2 = new Account();
        account2.setId(2L);
        account2.setAccountNumber("SAN987654321");
        account2.setAccountType(Account.AccountType.CURRENT);
        account2.setCustomer(customer);

        when(accountRepository.findByCustomerId(1L))
                .thenReturn(Arrays.asList(account, account2));

        List<Account> result = accountService.getAccountsByCustomer(1L);

        assertEquals(2, result.size());
        verify(accountRepository).findByCustomerId(1L);
    }

    @Test
    void getAccountsByCustomer_emptyList() {
        when(accountRepository.findByCustomerId(99L)).thenReturn(List.of());

        List<Account> result = accountService.getAccountsByCustomer(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBalance_returnsBalance() {
        when(accountRepository.findByAccountNumber("SAN123456789"))
                .thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance("SAN123456789");

        assertEquals(BigDecimal.valueOf(1000), balance);
    }

    @Test
    void getBalance_accountNotFound_throwsNotFound() {
        when(accountRepository.findByAccountNumber("SAN000000000"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> accountService.getBalance("SAN000000000")
        );
    }
}

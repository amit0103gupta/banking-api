package com.xbank.banking_api.controller;

import com.xbank.banking_api.model.Account;
import com.xbank.banking_api.model.Customer;
import com.xbank.banking_api.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
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
    void openAccount_returns201() throws Exception {
        when(accountService.openAccount(1L, Account.AccountType.SAVINGS))
                .thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .param("customerId", "1")
                        .param("accountType", "SAVINGS"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("SAN123456789"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));
    }

    @Test
    void openAccount_customerNotFound_returns404() throws Exception {
        when(accountService.openAccount(99L, Account.AccountType.SAVINGS))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        mockMvc.perform(post("/api/accounts")
                        .param("customerId", "99")
                        .param("accountType", "SAVINGS"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccount_returns200() throws Exception {
        when(accountService.getAccountByNumber("SAN123456789")).thenReturn(account);

        mockMvc.perform(get("/api/accounts/SAN123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("SAN123456789"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        when(accountService.getAccountByNumber("SAN000000000"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(get("/api/accounts/SAN000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccountsByCustomer_returns200() throws Exception {
        Account account2 = new Account();
        account2.setId(2L);
        account2.setAccountNumber("SAN987654321");
        account2.setAccountType(Account.AccountType.CURRENT);
        account2.setBalance(BigDecimal.valueOf(500));

        when(accountService.getAccountsByCustomer(1L))
                .thenReturn(Arrays.asList(account, account2));

        mockMvc.perform(get("/api/accounts/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAccountsByCustomer_empty_returns200() throws Exception {
        when(accountService.getAccountsByCustomer(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts/customer/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getBalance_returns200() throws Exception {
        when(accountService.getBalance("SAN123456789"))
                .thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/api/accounts/SAN123456789/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    void getBalance_accountNotFound_returns404() throws Exception {
        when(accountService.getBalance("SAN000000000"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(get("/api/accounts/SAN000000000/balance"))
                .andExpect(status().isNotFound());
    }
}

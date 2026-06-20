package com.xbank.banking_api.controller;

import com.xbank.banking_api.model.Account;
import com.xbank.banking_api.model.Transaction;
import com.xbank.banking_api.service.TransactionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    private Transaction depositTx;
    private Transaction withdrawTx;
    private Transaction transferTx;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("SAN111111111");

        depositTx = new Transaction();
        depositTx.setId(1L);
        depositTx.setType(Transaction.TransactionType.DEPOSIT);
        depositTx.setAmount(BigDecimal.valueOf(500));
        depositTx.setToAccount(account);
        depositTx.setDescription("Salary deposit");

        withdrawTx = new Transaction();
        withdrawTx.setId(2L);
        withdrawTx.setType(Transaction.TransactionType.WITHDRAWAL);
        withdrawTx.setAmount(BigDecimal.valueOf(200));
        withdrawTx.setFromAccount(account);
        withdrawTx.setDescription("ATM withdrawal");

        transferTx = new Transaction();
        transferTx.setId(3L);
        transferTx.setType(Transaction.TransactionType.TRANSFER);
        transferTx.setAmount(BigDecimal.valueOf(300));
        transferTx.setDescription("Rent");
    }

    @Test
    void deposit_returns201() throws Exception {
        when(transactionService.deposit(eq("SAN111111111"),
                eq(BigDecimal.valueOf(500)), eq("Salary deposit")))
                .thenReturn(depositTx);

        mockMvc.perform(post("/api/transactions/deposit")
                        .param("accountNumber", "SAN111111111")
                        .param("amount", "500")
                        .param("description", "Salary deposit"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500));
    }

    @Test
    void deposit_defaultDescription_returns201() throws Exception {
        when(transactionService.deposit(eq("SAN111111111"),
                eq(BigDecimal.valueOf(500)), eq("Deposit")))
                .thenReturn(depositTx);

        mockMvc.perform(post("/api/transactions/deposit")
                        .param("accountNumber", "SAN111111111")
                        .param("amount", "500"))
                .andExpect(status().isCreated());
    }

    @Test
    void deposit_accountNotFound_returns404() throws Exception {
        when(transactionService.deposit(eq("SAN000000000"),
                any(BigDecimal.class), any(String.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(post("/api/transactions/deposit")
                        .param("accountNumber", "SAN000000000")
                        .param("amount", "100"))
                .andExpect(status().isNotFound());
    }

    @Test
    void withdraw_returns201() throws Exception {
        when(transactionService.withdraw(eq("SAN111111111"),
                eq(BigDecimal.valueOf(200)), eq("ATM withdrawal")))
                .thenReturn(withdrawTx);

        mockMvc.perform(post("/api/transactions/withdraw")
                        .param("accountNumber", "SAN111111111")
                        .param("amount", "200")
                        .param("description", "ATM withdrawal"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    void withdraw_insufficientBalance_returns400() throws Exception {
        when(transactionService.withdraw(eq("SAN111111111"),
                any(BigDecimal.class), any(String.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));

        mockMvc.perform(post("/api/transactions/withdraw")
                        .param("accountNumber", "SAN111111111")
                        .param("amount", "999999")
                        .param("description", "Big withdrawal"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_returns201() throws Exception {
        when(transactionService.transfer(eq("SAN111111111"),
                eq("SAN222222222"), eq(BigDecimal.valueOf(300)), eq("Rent")))
                .thenReturn(transferTx);

        mockMvc.perform(post("/api/transactions/transfer")
                        .param("fromAccountNumber", "SAN111111111")
                        .param("toAccountNumber", "SAN222222222")
                        .param("amount", "300")
                        .param("description", "Rent"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(300));
    }

    @Test
    void transfer_sameAccount_returns400() throws Exception {
        when(transactionService.transfer(eq("SAN111111111"),
                eq("SAN111111111"), any(BigDecimal.class), any(String.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cannot transfer to the same account"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .param("fromAccountNumber", "SAN111111111")
                        .param("toAccountNumber", "SAN111111111")
                        .param("amount", "100")
                        .param("description", "Self"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHistory_returns200() throws Exception {
        when(transactionService.getTransactionHistory("SAN111111111"))
                .thenReturn(Arrays.asList(depositTx, withdrawTx));

        mockMvc.perform(get("/api/transactions/history/SAN111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getHistory_empty_returns200() throws Exception {
        when(transactionService.getTransactionHistory("SAN111111111"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/transactions/history/SAN111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

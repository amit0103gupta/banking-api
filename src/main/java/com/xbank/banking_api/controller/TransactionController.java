package com.xbank.banking_api.controller;

import com.xbank.banking_api.model.Transaction;
import com.xbank.banking_api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Transaction", description = "Banking transaction endpoints")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Deposit money into an account")
    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Deposit") String description) {
        return new ResponseEntity<>(
                transactionService.deposit(accountNumber, amount, description),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Withdraw money from an account")
    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Withdrawal") String description) {
        return new ResponseEntity<>(
                transactionService.withdraw(accountNumber, amount, description),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Transfer money between accounts")
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Transfer") String description) {
        return new ResponseEntity<>(
                transactionService.transfer(fromAccountNumber, toAccountNumber,
                        amount, description),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Get transaction history for an account")
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<Transaction>> getHistory(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));
    }
}

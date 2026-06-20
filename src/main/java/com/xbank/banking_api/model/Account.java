package com.xbank.banking_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public enum AccountType   { SAVINGS, CURRENT }
    public enum AccountStatus { ACTIVE, INACTIVE, CLOSED }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Customer getCustomer() { return customer; }

    // Setters
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}

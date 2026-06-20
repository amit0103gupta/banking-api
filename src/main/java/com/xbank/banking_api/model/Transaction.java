package com.xbank.banking_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.SUCCESS;

    public enum TransactionType   { DEPOSIT, WITHDRAWAL, TRANSFER }
    public enum TransactionStatus { SUCCESS, FAILED }

    // Getters
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Account getFromAccount() { return fromAccount; }
    public Account getToAccount() { return toAccount; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }

    // Setters
    public void setType(TransactionType type) { this.type = type; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TransactionStatus status) { this.status = status; }
}

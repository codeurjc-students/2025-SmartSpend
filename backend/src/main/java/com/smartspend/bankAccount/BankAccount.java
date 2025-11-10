package com.smartspend.bankAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.smartspend.transaction.Transaction;
import com.smartspend.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bankAccounts")
@Getter @Setter
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private BigDecimal currentBalance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Transaction> accountTransactions;

    public BankAccount() {}
    
    public BankAccount(User user, String name, BigDecimal initialBalance) {
        this.user = user;
        this.accountName = name;
        this.currentBalance = initialBalance;
    }
}

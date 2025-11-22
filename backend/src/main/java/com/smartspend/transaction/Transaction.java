package com.smartspend.transaction;

import java.math.BigDecimal;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.category.Category;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "transactions")
@Getter @Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(length = 100)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private java.time.LocalDate date;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // INCOME o EXPENSE

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recurrence recurrence = Recurrence.NONE;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    @JsonManagedReference
    private BankAccount account;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    private Category category;



    // Constructors
    public Transaction() {}
     
    public Transaction(String title, String description, BigDecimal amount, 
                      java.time.LocalDate date, TransactionType type, 
                      Category category, Recurrence recurrence, BankAccount account) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.recurrence = recurrence;
        this.account = account;
    }
}

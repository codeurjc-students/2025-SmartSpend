package com.smartspend.transaction;




import java.math.BigDecimal;

import jakarta.persistence.*;
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
    private Type category;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recurrence recurrence = Recurrence.NONE; // recurrencia
    
    
    public enum Type {EXPENSE, INCOME}
    public enum Recurrence {NONE, DAILY, WEEKLY, MONTHLY, YEARLY}
    public enum Category { FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, OTHER }

}

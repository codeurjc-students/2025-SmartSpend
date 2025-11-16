package com.smartspend.category;

import java.util.List;

import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter @Setter
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private String color = "#6c757d";

    @Column(length = 10, nullable = false)
    String icon ;
    
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // INCOME o EXPENSE

    @Column(nullable = false)
    private Boolean isDefault = false; // true para categorías del sistema

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // null para categorías por defecto

    @OneToMany(mappedBy = "category")
    @JsonBackReference
    private List<Transaction> transactions;

    // Constructors
    public Category() {}

    // Constructor para categorías por defecto
    public Category(String name, String description, String color, TransactionType type, String icon) {
        this.name = name;
        this.color = color;
        this.type = type;
        this.isDefault = true;
        this.user = null;
        this.icon = icon;
    }

    // Constructor para categorías personalizadas
    public Category(String name, String description, String color, TransactionType type, User user, String icon) {
        this.name = name;
        this.color = color;
        this.type = type;
        this.isDefault = false;
        this.user = user;
        this.icon = icon;
    }
}

package com.smartspend.transaction;

import java.math.BigDecimal;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.category.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "transactions")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    @JsonIgnore 
    private byte[] imageData;

    @Column(name = "image_type", length = 100)
    private String imageType;

    @Column(name = "image_name", length = 255)
    private String imageName;



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

    public boolean hasImage() {
        return this.imageData != null && this.imageData.length > 0;
    }

    public String getImageBase64() {
        if (hasImage()) {
            return java.util.Base64.getEncoder().encodeToString(this.imageData);
        }
        return null;
    }
}

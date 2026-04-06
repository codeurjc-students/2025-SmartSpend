package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.TransactionType;

public class CreateTransactionWithImageDto {
    private String title;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDate date;
    private Recurrence recurrence;
    private Long accountId;
    private Long categoryId;
    private MultipartFile imageFile;
    private BigDecimal personalAmount;
    private Boolean excludeFromStats;
    private List<DebtDto> debts;

    // Constructor vacío
    public CreateTransactionWithImageDto() {}

    // Getters y Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Recurrence getRecurrence() { return recurrence; }
    public void setRecurrence(Recurrence recurrence) { this.recurrence = recurrence; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }

    public BigDecimal getPersonalAmount() { return personalAmount; }
    public void setPersonalAmount(BigDecimal personalAmount) { this.personalAmount = personalAmount; }

    public Boolean getExcludeFromStats() { return excludeFromStats; }
    public void setExcludeFromStats(Boolean excludeFromStats) { this.excludeFromStats = excludeFromStats; }

    public List<DebtDto> getDebts() { return debts; }
    public void setDebts(List<DebtDto> debts) { this.debts = debts; }
}

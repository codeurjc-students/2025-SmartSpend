package com.smartspend.transaction;

public enum TransactionType {
    INCOME("Ingreso"), 
    EXPENSE("Gasto");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
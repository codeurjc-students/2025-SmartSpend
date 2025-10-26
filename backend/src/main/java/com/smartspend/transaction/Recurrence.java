package com.smartspend.transaction;

public enum Recurrence {
    NONE("Sin recurrencia"),
    DAILY("Diaria"), 
    WEEKLY("Semanal"), 
    MONTHLY("Mensual"), 
    YEARLY("Anual");
    
    private final String displayName;
    
    Recurrence(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
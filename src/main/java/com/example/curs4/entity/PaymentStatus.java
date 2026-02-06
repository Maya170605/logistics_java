package com.example.curs4.entity;

public enum PaymentStatus {
    PENDING("Ожидает оплаты"),
    PAID("Оплачено"),
    OVERDUE("Просрочено");
//    CANCELLED("Отменено");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
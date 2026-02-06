package com.example.curs4.entity;

public enum PaymentType {
    CUSTOMS_DUTY("Таможенная пошлина"),
    VAT("НДС"),
    EXCISE("Акциз"),
    FEE("Сбор"),
    PENALTY("Штраф"),
    STORAGE("Хранение"),
    TRANSPORT("Транспорт"),
    OTHER("Прочее");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
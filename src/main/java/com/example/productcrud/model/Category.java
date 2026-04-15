package com.example.productcrud.model;

public enum Category {
    ELEKTRONIK("Elektronik"),
    BUKU("Buku"),
    MAKANAN("Makanan"),
    PAKAIAN("Pakaian");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

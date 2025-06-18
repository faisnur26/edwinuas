package com.project.edwinuas_nasmoco.api.model;

public class Category {
    private String nama;
    private int iconResId; // drawable ID

    public Category(String nama, int iconResId) {
        this.nama = nama;
        this.iconResId = iconResId;
    }

    public String getNama() {
        return nama;
    }

    public int getIconResId() {
        return iconResId;
    }
}

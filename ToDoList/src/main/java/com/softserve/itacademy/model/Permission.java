package com.softserve.itacademy.model;

public enum Permission {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}

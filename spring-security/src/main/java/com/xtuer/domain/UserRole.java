package com.xtuer.domain;

public class UserRole {
    private int id;
    private String role;

    public UserRole() {

    }

    public UserRole(String role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
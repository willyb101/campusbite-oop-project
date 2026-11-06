package com.zut.campusbite;

/** Shared data and behaviour for people who use CampusBite. */
public abstract class User {
    private int userId;
    private String name;
    private String phone;
    private String email;

    protected User(int userId, String name, String phone, String email) {
        this.userId = userId;
        this.name = requireText(name, "name");
        this.phone = requireText(phone, "phone");
        this.email = requireText(email, "email");
    }

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = requireText(name, "name"); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = requireText(phone, "phone"); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = requireText(email, "email"); }
}

package io.notfound.counsel_back.user.entity;

/**
 * 사용자 역할 정의
 * ADMIN: 관리자 권한
 * USER: 일반 사용자 권한
 */
public enum UserRole {
    ADMIN("admin"),
    USER("user");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
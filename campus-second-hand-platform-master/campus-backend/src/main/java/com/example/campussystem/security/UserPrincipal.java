package com.example.campussystem.security;

import java.security.Principal;

/**
 * 用户主体类
 */
public class UserPrincipal implements Principal {
    
    private final Long userId;
    private final String username;
    private final String displayName;

    public UserPrincipal(Long userId, String username, String displayName) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
    }

    @Override
    public String getName() { return username; }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() {
        return "UserPrincipal{userId=" + userId + ", username='" + username + "'}";
    }
}

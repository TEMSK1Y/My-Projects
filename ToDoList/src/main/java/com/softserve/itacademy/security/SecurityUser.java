package com.softserve.itacademy.security;

import com.softserve.itacademy.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
public class SecurityUser implements UserDetails {

    private long id;
    private final String username;
    private String firstName;
    private final String password;
    private Collection<? extends GrantedAuthority> authorities;
    private final boolean isActive;

    public SecurityUser(Long id, String username, String firstName, String password,
                        Collection<? extends GrantedAuthority> authorities, boolean isActive) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.password = password;
        this.authorities = authorities;
        this.isActive = isActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public static UserDetails fromUser(User user) {
        return new SecurityUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getPassword(),
                user.getRole().getAuthorities(),
                true
        );
    }
}

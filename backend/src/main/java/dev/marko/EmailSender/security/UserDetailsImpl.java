package dev.marko.EmailSender.security;

import dev.marko.EmailSender.entities.Role;
import dev.marko.EmailSender.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRole();
        if (role == null) return Collections.emptyList();

        // Spring Security expects ROLE_ prefix
        String roleName = "ROLE_" + role.name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));

    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // ili tvoja logika
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // using Boolean class to turn null values to false and to avoid NullPointerException
        return Boolean.TRUE.equals(user.getEnabled());
    }
}
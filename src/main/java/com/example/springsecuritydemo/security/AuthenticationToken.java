package com.example.springsecuritydemo.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String username;
    private final Collection<? extends GrantedAuthority> authoritiesList;

    public AuthenticationToken(
        String username,
        Collection<? extends GrantedAuthority> authoritiesList) {
        super(username, "", authoritiesList);
        this.username = username;
        this.authoritiesList = authoritiesList;
    }

    public String getUsername() {
        return username;
    }

    public Collection<? extends GrantedAuthority> getAuthoritiesList() {
        return authoritiesList;
    }
}

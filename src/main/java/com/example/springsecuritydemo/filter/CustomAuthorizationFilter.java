package com.example.springsecuritydemo.filter;

import com.example.springsecuritydemo.manager.JwtManager;
import com.example.springsecuritydemo.security.AuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        try {
            AuthenticationToken authenticationToken = this.authenticateJwt(request);
            // 提供給後續API可能會操作到權限相關的人使用authenticationToken.
            SecurityContextHolder.getContext()
                .setAuthentication(
                    authenticationToken
                );
            doFilter(request, response, filterChain);
        } catch (RuntimeException e) {
            this.handleAccessException(response, e);
        }
    }

    public AuthenticationToken authenticateJwt(HttpServletRequest request) {
        String token = Optional.ofNullable(request.getHeader("Authorization"))
            .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("請登入後獲得授權"));
        //檢測是否有token.
        Jws<Claims> jwt;
        // token驗證
        try {
            jwt = JwtManager.getTokenClaim(token);
        } catch (Exception ex) {
            throw new RuntimeException("授權有問題.");
        }

        String username = String.valueOf(jwt.getBody().get("username"));
        List<String> roles = (List<String>) jwt.getBody().get("roles");

        Collection<SimpleGrantedAuthority> authorities  = new ArrayList<>();
        roles.forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role));
        });

        return new AuthenticationToken(username, authorities);
    }

    private void handleAccessException(
        HttpServletResponse response,
        Exception e) throws IOException {
        String errMsg = null;

        if (e instanceof RuntimeException) {
            errMsg = String.format("登入錯誤: %s", e.getMessage());
            log.error("Error logging in: {}", errMsg);
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, String> error = new HashMap<>();
        error.put("error_message", errMsg);

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}

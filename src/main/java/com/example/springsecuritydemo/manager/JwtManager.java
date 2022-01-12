package com.example.springsecuritydemo.manager;

import com.example.springsecuritydemo.domain.Role;
import com.example.springsecuritydemo.domain.User;
import com.example.springsecuritydemo.dto.TokenDTO;
import com.example.springsecuritydemo.repo.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.crypto.MacProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtManager {
    private final UserRepository userRepository;

    private static final Key TOKEN_HASH_SALT = MacProvider.generateKey();
    public static final String TOKEN_PREFIX = "Bearer"; // Token前缀

    /*
     * 解析Token 失敗則為非法Token
     */
    public static Jws<Claims> getTokenClaim(String token) {
        Jws<Claims> jwt;
        try {
            jwt = Jwts.parser()
                .setSigningKey(TOKEN_HASH_SALT)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""));
        } catch (SignatureException e) {
            throw new RuntimeException("授權憑證錯誤");
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("授權憑證過期，請重新申請");
        } catch (Exception e) {
            throw new RuntimeException("授權無法認證");
        }
        return jwt;
    }

    public TokenDTO generateTokenDTO(String username, String password) {
        // 生成JWT
        TokenDTO tokenDTO = new TokenDTO(this.generateUserToken(username, password),
            this.generateRefreshToken(username));

        return tokenDTO;
    }

    private String generateUserToken(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            log.info("User found in the database: {}", username);
        }

        if (user.getPassword().equals(password)) {

            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            user.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
            });

            // 生成JWT
            return Jwts.builder()
                .claim("username", username)
                .claim("roles", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, TOKEN_HASH_SALT).compact();
        } else {
            throw new IllegalArgumentException("密碼驗證不正確");
        }
    }

    private String generateRefreshToken(String username) {
        // 生成JWT
        return Jwts.builder()
            .claim("username", username)
            .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
            .signWith(SignatureAlgorithm.HS256, TOKEN_HASH_SALT).compact();
    }

}

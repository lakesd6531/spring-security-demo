package com.example.springsecuritydemo.security;

import com.example.springsecuritydemo.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        try {
            http.cors()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(GET, "/api/user/**").hasAnyAuthority("ROLE_USER")
                .antMatchers(POST, "/api/user/save/**").hasAnyAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilterAt(
                    new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class
                );
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void configure(WebSecurity web) {
        // 使用ignoring會繞過filter.
        web.ignoring().antMatchers(
            HttpMethod.GET,
            "/api/testToken"
        );
        web.ignoring().antMatchers(
            HttpMethod.POST,
            "/"
        );
        web.ignoring().antMatchers(
            HttpMethod.PUT,
            "/"
        );
        web.ignoring().antMatchers(
            HttpMethod.DELETE,
            "/"
        );
        web.ignoring().antMatchers(
            HttpMethod.OPTIONS,
            "/"
        );
    }
}

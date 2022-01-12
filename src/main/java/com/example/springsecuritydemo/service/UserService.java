package com.example.springsecuritydemo.service;

import com.example.springsecuritydemo.domain.Role;
import com.example.springsecuritydemo.domain.User;
import com.example.springsecuritydemo.dto.TokenDTO;

import java.util.List;

public interface UserService {
    User saveUser(User user);
    Role saveRole(Role role);
    void addRoleToUser(String username, String roleName);
    User getUser(String username);
    List<User> getUsers();
    TokenDTO createToken(String username, String password);
}

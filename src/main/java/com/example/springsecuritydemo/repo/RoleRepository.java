package com.example.springsecuritydemo.repo;

import com.example.springsecuritydemo.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}

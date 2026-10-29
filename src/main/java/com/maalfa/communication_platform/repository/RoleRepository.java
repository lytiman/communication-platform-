package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}

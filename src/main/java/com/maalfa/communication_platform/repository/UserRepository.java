//package com.maalfa.communication_platform.repository;
//
//import com.maalfa.communication_platform.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    Optional<User> findByUsername(String username);
//
//    Optional<User> findByEmail(String email);
//
//    boolean existsByUsername(String username);
//
//    boolean existsByEmail(String email);
//
//    // Used by UserService.searchUsers() — lets a client search by either
//    // username or display name in one query, case-insensitive.
//    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(
//            String username, String fullName);
//}
package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Used by UserService.searchUsers() — lets a client search by either
    // username or display name in one query, case-insensitive.
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String username, String fullName);

    // Eagerly fetches roles AND each role's privileges in one query, so
    // UserPrincipal.getAuthorities() never touches a lazy collection after
    // the session closes. Used specifically for authentication
    // (CustomUserDetailsService) — everything getAuthorities() needs is
    // already loaded by the time this returns, regardless of when/where
    // in the filter chain getAuthorities() ends up being called.
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.privileges " +
            "WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmailWithRolesAndPrivileges(@Param("usernameOrEmail") String usernameOrEmail);
}
package com.maalfa.communication_platform.security;

import com.maalfa.communication_platform.entity.Privilege;
import com.maalfa.communication_platform.entity.Role;
import com.maalfa.communication_platform.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps a User entity so Spring Security can work with it. Authorities
 * are flattened from BOTH levels of the permission model: role names
 * themselves (e.g. "ROLE_ADMIN" — for @PreAuthorize("hasRole(...)"))
 * AND each role's individual privileges (e.g. "BAN_USER" — for
 * @PreAuthorize("hasAuthority(...)")). This lets controllers/services
 * check whichever granularity fits the situation.
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();

        Stream<String> roleNames = roles.stream()
                .map(Role::getName);

        Stream<String> privilegeNames = roles.stream()
                .flatMap(role -> role.getPrivileges().stream())
                .map(Privilege::getName);

        return Stream.concat(roleNames, privilegeNames)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}

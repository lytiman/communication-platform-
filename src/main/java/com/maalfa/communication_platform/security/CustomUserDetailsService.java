//package com.maalfa.communication_platform.security;
//
//import com.maalfa.communication_platform.entity.User;
//import com.maalfa.communication_platform.repository.UserRepository;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    public CustomUserDetailsService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    // "username" here is really "usernameOrEmail" — allows login with either,
//    // matching LoginRequest.usernameOrEmail. Spring Security's interface
//    // names the parameter "username" regardless of what it actually contains.
//    @Override
//    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
//        User user = userRepository.findByUsername(usernameOrEmail)
//                .or(() -> userRepository.findByEmail(usernameOrEmail))
//                .orElseThrow(() -> new UsernameNotFoundException(
//                        "No user found with username or email: " + usernameOrEmail));
//        return new UserPrincipal(user);
//    }
//}

package com.maalfa.communication_platform.security;

import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // "username" here is really "usernameOrEmail" — allows login with either,
    // matching LoginRequest.usernameOrEmail. Spring Security's interface
    // names the parameter "username" regardless of what it actually contains.
    //
    // Uses the fetch-join query (not findByUsername/findByEmail) so roles
    // and privileges are already loaded before this method returns — avoids
    // LazyInitializationException when UserPrincipal.getAuthorities() runs
    // later in JwtAuthenticationFilter, potentially outside the original
    // Hibernate session.
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmailWithRolesAndPrivileges(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with username or email: " + usernameOrEmail));
        return new UserPrincipal(user);
    }
}
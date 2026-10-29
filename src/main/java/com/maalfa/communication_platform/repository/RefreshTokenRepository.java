package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.RefreshToken;
import com.maalfa.communication_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // A user can have several tokens (one per device), so this returns a list.
    List<RefreshToken> findAllByUser(User user);

    // Used on logout for a single device.
    void deleteByToken(String token);

    // Used on "log out everywhere" / password change / account compromise.
    void deleteAllByUser(User user);
}

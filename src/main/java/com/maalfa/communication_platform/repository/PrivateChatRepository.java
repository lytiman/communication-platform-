package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.PrivateChat;
import com.maalfa.communication_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, Long> {

    // Service layer is expected to store userOne/userTwo in a consistent
    // order (e.g. lower id first), but this query checks both orderings
    // defensively so callers never have to worry about which side is which.
    @Query("SELECT pc FROM PrivateChat pc WHERE " +
            "(pc.userOne = :userA AND pc.userTwo = :userB) OR " +
            "(pc.userOne = :userB AND pc.userTwo = :userA)")
    Optional<PrivateChat> findByUsers(@Param("userA") User userA, @Param("userB") User userB);

    // All private chats a given user participates in (for the chat list screen).
    @Query("SELECT pc FROM PrivateChat pc WHERE pc.userOne = :user OR pc.userTwo = :user " +
            "ORDER BY pc.lastMessageAt DESC")
    List<PrivateChat> findAllForUser(@Param("user") User user);
}

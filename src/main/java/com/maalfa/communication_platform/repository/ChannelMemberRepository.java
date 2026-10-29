package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.Channel;
import com.maalfa.communication_platform.entity.ChannelMember;
import com.maalfa.communication_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
 public interface ChannelMemberRepository extends JpaRepository <ChannelMember, Long>{
    Optional<ChannelMember> findByChannelAndUser(Channel channel,User user);
    boolean existsByChannelAndUser(Channel channel, User user);
    List<ChannelMember> findAllByChannel(Channel channel);
    List<ChannelMember> findAllByUser(User user);
 }
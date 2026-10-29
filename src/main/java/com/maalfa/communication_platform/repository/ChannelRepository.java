package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.Channel;
import com.maalfa.communication_platform.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ChannelRepository extends JpaRepository <Channel, Long>{
    List<Channel> findAllByGroup(Group group);
}

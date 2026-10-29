package com.maalfa.communication_platform.repository;
import com.maalfa.communication_platform.entity.Group;
import com.maalfa.communication_platform.entity.GroupMember;
import com.maalfa.communication_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface GroupMemberRepository extends JpaRepository <GroupMember, Long>{
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    boolean existsByGroupAndUser(Group group, User user);
    //All groups a given user belongs to (with membership row/role attached).
    List<GroupMember> findAllByUser(User user);
    // All members of a given group.
    List<GroupMember> findAllByGroup(Group group);
}

package com.maalfa.communication_platform.repository;
import com.maalfa.communication_platform.entity.Group;
import  com.maalfa.communication_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GroupRepository extends JpaRepository<Group,Long> {
    List<Group> findAllByCreatedBy(User creator);
}

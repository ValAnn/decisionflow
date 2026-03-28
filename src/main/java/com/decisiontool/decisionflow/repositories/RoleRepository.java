package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.Role;
import com.decisiontool.decisionflow.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}

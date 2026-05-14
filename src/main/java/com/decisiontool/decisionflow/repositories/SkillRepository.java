package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.AnalystProfile;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByCategory(String category);

    Optional<Skill> findByNameIgnoreCase(String name);
}
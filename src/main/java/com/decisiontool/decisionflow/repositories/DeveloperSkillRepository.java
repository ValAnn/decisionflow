package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.DeveloperSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeveloperSkillRepository extends JpaRepository<DeveloperSkill, Long> {
    
    // Самый важный метод: найти разработчиков, знающих конкретный скилл, 
    // и отсортировать их по уровню владения (от профи к новичкам)
    // @Query("SELECT ds FROM DeveloperSkill ds WHERE ds.skill.id = :skillId ORDER BY ds.proficiencyLevel DESC")
    // List<DeveloperSkill> findTopExpertsForSkill(Long skillId);
}
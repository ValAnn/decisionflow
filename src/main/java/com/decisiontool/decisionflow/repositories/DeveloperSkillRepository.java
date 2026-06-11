package com.decisiontool.decisionflow.repositories;
import com.decisiontool.decisionflow.entities.DeveloperSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DeveloperSkillRepository extends JpaRepository<DeveloperSkill, Long> {
}

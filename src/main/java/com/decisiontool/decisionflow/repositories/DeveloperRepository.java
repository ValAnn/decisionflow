package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.AnalystProfile;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<DeveloperProfile, Long> {
    // Найти всех разработчиков по основной специализации (Java, Vue и т.д.)
    List<DeveloperProfile> findAllBySpecializationId(Long skillId);

    Optional<DeveloperProfile> findByUserId(Long userId);
}
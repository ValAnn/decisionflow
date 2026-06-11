package com.decisiontool.decisionflow.services;
import com.decisiontool.decisionflow.entities.Skill;
import com.decisiontool.decisionflow.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    public Skill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Навык не найден"));
    }
}

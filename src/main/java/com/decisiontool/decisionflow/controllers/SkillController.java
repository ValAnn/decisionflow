package com.decisiontool.decisionflow.controllers;

import com.decisiontool.decisionflow.entities.Skill;
import com.decisiontool.decisionflow.services.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public List<Skill> getAll() {
        return skillService.getAllSkills();
    }

    @GetMapping("/{id}")
    public Skill getById(@PathVariable Long id) {
        return skillService.getSkillById(id);
    }
}
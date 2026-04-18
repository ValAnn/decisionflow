package com.decisiontool.decisionflow.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decisiontool.decisionflow.dtos.DepartmentTeamDTO;
import com.decisiontool.decisionflow.entities.Department;
import com.decisiontool.decisionflow.repositories.DepartmentRepository;
import com.decisiontool.decisionflow.services.DepartmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    //private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;

    // Получить все департаменты (нужно для выпадающих списков на фронте)
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    // Получить один департамент по ID
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    // Создать новый департамент (только для админа или через инициализацию базы)
    @PostMapping
    public Department createDepartment(@RequestBody Department department) {
        return departmentService.createDepartment(department);
    }

    @GetMapping("/teams")
    public ResponseEntity<List<DepartmentTeamDTO>> getTeams() {
        List<DepartmentTeamDTO> teams = departmentService.getDepartmentsTeams();
        return ResponseEntity.ok(teams);
    }
}
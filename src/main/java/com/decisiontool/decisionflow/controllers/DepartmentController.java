package com.decisiontool.decisionflow.controllers;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.decisiontool.decisionflow.dtos.DepartmentTeamDTO;
import com.decisiontool.decisionflow.dtos.DepartmentTopUsersDTO;
import com.decisiontool.decisionflow.entities.Department;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.DepartmentRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import com.decisiontool.decisionflow.services.DepartmentService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;
    private final TaskRepository taskRepository;

    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }
    @PostMapping
    public Department createDepartment(@RequestBody Department department) {
        return departmentService.createDepartment(department);
    }
    @GetMapping("/teams")
    public ResponseEntity<List<DepartmentTeamDTO>> getTeams() {
        List<DepartmentTeamDTO> teams = departmentService.getDepartmentsTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/top-staff")
    public ResponseEntity<List<DepartmentTopUsersDTO>> getDepartmentsTopStaff() {
        List<Department> departments = departmentRepository.findAll();

        List<DepartmentTopUsersDTO> result = departments.stream().map(dept -> {
            // Репозиторий теперь сразу отдает List<String>
            List<String> analysts = taskRepository.findTopAnalystNamesByDepartment(dept.getId());
            List<String> developers = taskRepository.findTopDeveloperNamesByDepartment(dept.getId());

            return new DepartmentTopUsersDTO(
                    dept.getId(),
                    dept.getName(),
                    analysts,
                    developers
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}

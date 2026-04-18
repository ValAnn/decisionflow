package com.decisiontool.decisionflow.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.decisiontool.decisionflow.dtos.DepartmentTeamDTO;
import com.decisiontool.decisionflow.dtos.UserStatsDTO;
import com.decisiontool.decisionflow.entities.Department;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.DepartmentRepository;
import com.decisiontool.decisionflow.repositories.TaskRepository;
import com.decisiontool.decisionflow.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    // Получить все департаменты (нужно для выпадающих списков на фронте)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public ResponseEntity<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }
    
    public List<DepartmentTeamDTO> getDepartmentsTeams() {
        return departmentRepository.findAll().stream().map(dept -> {
            DepartmentTeamDTO dto = new DepartmentTeamDTO(null, null, null, null);
            dto.setDepartmentId(dept.getId());
            dto.setDepartmentName(dept.getName());

            // Получаем топ аналитиков (Role ID = 3)
            dto.setTopAnalysts(fetchTopUsers(dept.getId(), 3L));

            // Получаем топ разработчиков (Role ID = 4)
            dto.setTopDevelopers(fetchTopUsers(dept.getId(), 4L));

            return dto;
        }).collect(Collectors.toList());
    }

    private List<UserStatsDTO> fetchTopUsers(Long deptId, Long roleId) {
        // Вызываем метод из репозитория, который мы обсуждали ранее
        List<User> topUsers = userRepository.findTopUsersByDepartmentAndRole(deptId, roleId);
        
        return topUsers.stream().map(user -> {
            Double percentage = taskRepository.getCompletionPercentage(deptId, user.getId());
            // Если задач еще нет, процент будет 0.0
            return new UserStatsDTO(user.getFullName(), percentage != null ? percentage : 0.0);
        }).collect(Collectors.toList());
    }
}
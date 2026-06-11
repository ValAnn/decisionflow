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
            dto.setTopAnalysts(fetchTopUsers(dept.getId(), 3L));
            dto.setTopDevelopers(fetchTopUsers(dept.getId(), 4L));
            return dto;
        }).collect(Collectors.toList());
    }
    private List<UserStatsDTO> fetchTopUsers(Long deptId, Long roleId) {
        List<User> topUsers = userRepository.findTopUsersByDepartmentAndRole(deptId, roleId);
        return topUsers.stream().map(user -> {
            Double percentage = taskRepository.getCompletionPercentage(deptId, user.getId());
            return new UserStatsDTO(user.getFullName(), percentage != null ? percentage : 0.0);
        }).collect(Collectors.toList());
    }
}

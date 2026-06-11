package com.decisiontool.decisionflow.services;
import com.decisiontool.decisionflow.entities.Role;
import com.decisiontool.decisionflow.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + name));
    }
}

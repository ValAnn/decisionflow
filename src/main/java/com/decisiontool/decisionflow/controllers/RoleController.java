package com.decisiontool.decisionflow.controllers;

import com.decisiontool.decisionflow.entities.Role;
import com.decisiontool.decisionflow.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<Role> getRoles() {
        return roleService.getAllRoles();
    }
}
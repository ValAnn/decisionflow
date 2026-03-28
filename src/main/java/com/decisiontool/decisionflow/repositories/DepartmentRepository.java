package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.Task;
import com.decisiontool.decisionflow.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
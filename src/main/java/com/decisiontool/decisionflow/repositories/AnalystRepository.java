package com.decisiontool.decisionflow.repositories;

import com.decisiontool.decisionflow.entities.AnalystProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalystRepository extends JpaRepository<AnalystProfile, Long> {
}
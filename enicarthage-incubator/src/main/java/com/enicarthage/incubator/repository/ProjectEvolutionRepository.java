package com.enicarthage.incubator.repository;

import com.enicarthage.incubator.model.ProjectEvolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectEvolutionRepository extends JpaRepository<ProjectEvolution, Long> {
    List<ProjectEvolution> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}

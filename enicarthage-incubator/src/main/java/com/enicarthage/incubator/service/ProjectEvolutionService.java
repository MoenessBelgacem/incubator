package com.enicarthage.incubator.service;

import com.enicarthage.incubator.dto.request.ProjectEvolutionRequest;
import com.enicarthage.incubator.dto.response.ProjectEvolutionResponse;
import com.enicarthage.incubator.model.Application;
import com.enicarthage.incubator.model.ProjectEvolution;
import com.enicarthage.incubator.repository.ApplicationRepository;
import com.enicarthage.incubator.repository.ProjectEvolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectEvolutionService {
    private final ProjectEvolutionRepository evolutionRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public ProjectEvolutionResponse addEvolution(Long applicationId, String userEmail, ProjectEvolutionRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable"));

        // Verify the user is the candidate of this application
        if (!application.getCandidate().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à ajouter une évolution pour ce projet");
        }

        ProjectEvolution evolution = ProjectEvolution.builder()
                .application(application)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        ProjectEvolution saved = evolutionRepository.save(evolution);
        return mapToResponse(saved);
    }

    public List<ProjectEvolutionResponse> getEvolutionsByApplication(Long applicationId) {
        return evolutionRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProjectEvolutionResponse mapToResponse(ProjectEvolution evolution) {
        return ProjectEvolutionResponse.builder()
                .id(evolution.getId())
                .applicationId(evolution.getApplication().getId())
                .title(evolution.getTitle())
                .content(evolution.getContent())
                .createdAt(evolution.getCreatedAt())
                .build();
    }
}

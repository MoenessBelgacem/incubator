package com.enicarthage.incubator.service;

import com.enicarthage.incubator.dto.request.EvaluationRequest;
import com.enicarthage.incubator.exception.ResourceNotFoundException;
import com.enicarthage.incubator.model.*;
import com.enicarthage.incubator.repository.ApplicationRepository;
import com.enicarthage.incubator.repository.EvaluationRepository;
import com.enicarthage.incubator.repository.ProjectRepository;
import com.enicarthage.incubator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Evaluation evaluate(EvaluationRequest request, String evaluatorEmail) {
        User evaluator = userRepository.findByEmail(evaluatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluateur introuvable"));

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidature introuvable"));

        Round currentRound = application.getCurrentRound();
        if (currentRound == null) {
            throw new IllegalStateException("Aucun round n'est associé à cette candidature.");
        }

        if (currentRound.getStatus() == RoundStatus.UPCOMING) {
            throw new IllegalStateException("L'évaluation n'est pas encore ouverte (Phase de soumission en cours).");
        }

        if (currentRound.getStatus() == RoundStatus.COMPLETED) {
            throw new IllegalStateException("L'évaluation est terminée pour ce round.");
        }

        // Only allow evaluation if status is ACTIVE
        if (currentRound.getStatus() != RoundStatus.ACTIVE) {
            throw new IllegalStateException("L'évaluation est impossible dans l'état actuel du round : " + currentRound.getStatus());
        }

        // Optional project if it exists
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        }

        // Find existing evaluation for this SPECIFIC round by this evaluator
        // Rule: One evaluation per evaluator per round
        Long roundId = application.getCurrentRound() != null ? application.getCurrentRound().getId() : null;
        
        Optional<Evaluation> existing = evaluationRepository.findAll().stream()
                .filter(e -> e.getApplication().getId().equals(application.getId()) 
                          && e.getEvaluator().getId().equals(evaluator.getId())
                          && ((e.getRound() == null && roundId == null) || (e.getRound() != null && e.getRound().getId().equals(roundId))))
                .findFirst();

        Evaluation evaluation;
        if (existing.isPresent()) {
            evaluation = existing.get();
            evaluation.setScore(request.getScore());
            evaluation.setComment(request.getComment());
            evaluation.setRecommendation(request.getRecommendation());
        } else {
            evaluation = Evaluation.builder()
                    .application(application)
                    .project(project)
                    .evaluator(evaluator)
                    .round(application.getCurrentRound()) 
                    .score(request.getScore())
                    .comment(request.getComment())
                    .recommendation(request.getRecommendation())
                    .build();
        }

        return evaluationRepository.save(evaluation);
    }

    public List<Evaluation> getEvaluationsByProject(Long projectId) {
        return evaluationRepository.findByProjectId(projectId);
    }

    public Double getAverageScore(Long projectId) {
        return evaluationRepository.findAverageScoreByProjectId(projectId).orElse(0.0);
    }
}

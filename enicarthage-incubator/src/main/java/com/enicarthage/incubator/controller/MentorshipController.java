package com.enicarthage.incubator.controller;

import com.enicarthage.incubator.dto.response.ApiResponse;
import com.enicarthage.incubator.dto.response.IncubatedApplicationResponse;
import com.enicarthage.incubator.model.Application;
import com.enicarthage.incubator.model.User;
import com.enicarthage.incubator.repository.UserRepository;
import com.enicarthage.incubator.service.MentorshipService;
import com.enicarthage.incubator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorshipController {

    private final MentorshipService mentorshipService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final com.enicarthage.incubator.repository.ApplicationRepository applicationRepository;

    @GetMapping("/applications")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse<List<IncubatedApplicationResponse>>> getMyIncubatedApplications(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User mentor = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Application> applications = mentorshipService.getIncubatedApplicationsForMentor(mentor.getId());

        List<IncubatedApplicationResponse> response = applications.stream().map(app -> {
            List<com.enicarthage.incubator.model.Project> projects = projectRepository.findByOwnerId(app.getCandidate().getId());
            String title = projects.isEmpty() ? "Projet non nommé" : projects.get(0).getTitle();
            String desc = projects.isEmpty() ? "" : projects.get(0).getDescription();
            
            return IncubatedApplicationResponse.builder()
                    .applicationId(app.getId())
                    .candidateName(app.getCandidate().getFirstName() + " " + app.getCandidate().getLastName())
                    .candidateEmail(app.getCandidate().getEmail())
                    .projectTitle(title)
                    .projectDescription(desc)
                    .status(app.getStatus().name())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Candidatures récupérées", response));
    }

    @PostMapping("/applications/{applicationId}/assign/{mentorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignMentor(
            @PathVariable Long applicationId,
            @PathVariable Long mentorId) {
        mentorshipService.assignMentor(applicationId, mentorId, userRepository, applicationRepository);
        return ResponseEntity.ok(ApiResponse.success("Mentor assigné avec succès", null));
    }
}

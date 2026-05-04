package com.enicarthage.incubator.controller;

import com.enicarthage.incubator.dto.request.ProjectEvolutionRequest;
import com.enicarthage.incubator.dto.response.ApiResponse;
import com.enicarthage.incubator.dto.response.ProjectEvolutionResponse;
import com.enicarthage.incubator.service.ProjectEvolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/evolutions")
@RequiredArgsConstructor
public class ProjectEvolutionController {

    private final ProjectEvolutionService evolutionService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ProjectEvolutionResponse>> addEvolution(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProjectEvolutionRequest request) {
        
        ProjectEvolutionResponse response = evolutionService.addEvolution(applicationId, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Évolution ajoutée avec succès", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectEvolutionResponse>>> getEvolutions(
            @PathVariable Long applicationId) {
        
        List<ProjectEvolutionResponse> evolutions = evolutionService.getEvolutionsByApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Évolutions récupérées", evolutions));
    }
}

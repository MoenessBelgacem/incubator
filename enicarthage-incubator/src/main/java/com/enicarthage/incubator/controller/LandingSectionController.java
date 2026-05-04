package com.enicarthage.incubator.controller;

import com.enicarthage.incubator.dto.response.ApiResponse;
import com.enicarthage.incubator.model.LandingSection;
import com.enicarthage.incubator.service.LandingSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/landing/sections")
@RequiredArgsConstructor
public class LandingSectionController {

    private final LandingSectionService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LandingSection>>> getVisibleSections() {
        return ResponseEntity.ok(ApiResponse.success("Sections visibles", service.getVisibleSections()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LandingSection>>> getAllSections() {
        return ResponseEntity.ok(ApiResponse.success("Toutes les sections", service.getAllSections()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LandingSection>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Section trouvée", service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LandingSection>> create(
            @RequestPart("section") LandingSection section,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success("Section créée", service.create(section, image)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LandingSection>> update(
            @PathVariable Long id,
            @RequestPart("section") LandingSection section,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success("Section mise à jour", service.update(id, section, image)));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reorder(@RequestBody List<Long> orderedIds) {
        service.reorder(orderedIds);
        return ResponseEntity.ok(ApiResponse.success("Ordre mis à jour", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Section supprimée", null));
    }
}

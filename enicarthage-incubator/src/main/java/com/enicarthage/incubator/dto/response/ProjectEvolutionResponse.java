package com.enicarthage.incubator.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectEvolutionResponse {
    private Long id;
    private Long applicationId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}

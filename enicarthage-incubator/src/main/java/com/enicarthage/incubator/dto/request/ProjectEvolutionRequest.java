package com.enicarthage.incubator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectEvolutionRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "Le contenu est obligatoire")
    private String content;
}

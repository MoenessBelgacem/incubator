package com.enicarthage.incubator.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncubatedApplicationResponse {
    private Long applicationId;
    private String candidateName;
    private String candidateEmail;
    private String projectTitle;
    private String projectDescription;
    private String status;
}

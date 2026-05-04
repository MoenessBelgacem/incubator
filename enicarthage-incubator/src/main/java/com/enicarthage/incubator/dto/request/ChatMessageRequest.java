package com.enicarthage.incubator.dto.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long applicationId;
    private String content;
}

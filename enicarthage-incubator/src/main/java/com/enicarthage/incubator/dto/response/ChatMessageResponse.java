package com.enicarthage.incubator.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long applicationId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private LocalDateTime timestamp;
}

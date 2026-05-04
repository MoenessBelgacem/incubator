package com.enicarthage.incubator.controller;

import com.enicarthage.incubator.dto.request.ChatMessageRequest;
import com.enicarthage.incubator.dto.response.ApiResponse;
import com.enicarthage.incubator.dto.response.ChatMessageResponse;
import com.enicarthage.incubator.model.Application;
import com.enicarthage.incubator.model.ChatMessage;
import com.enicarthage.incubator.model.User;
import com.enicarthage.incubator.repository.ApplicationRepository;
import com.enicarthage.incubator.repository.ChatMessageRepository;
import com.enicarthage.incubator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Authentication authentication) {
        if (authentication == null) return;
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User sender = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Application application = applicationRepository.findById(request.getApplicationId()).orElseThrow();

        ChatMessage message = ChatMessage.builder()
                .application(application)
                .sender(sender)
                .content(request.getContent())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(saved.getId())
                .applicationId(saved.getApplication().getId())
                .senderId(saved.getSender().getId())
                .senderName(saved.getSender().getFirstName() + " " + saved.getSender().getLastName())
                .senderRole(saved.getSender().getRole().name())
                .content(saved.getContent())
                .timestamp(saved.getTimestamp())
                .build();

        // Broadcast to the specific application's chat room
        messagingTemplate.convertAndSend("/topic/chat/" + request.getApplicationId(), response);
    }

    @GetMapping("/api/applications/{applicationId}/chat")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(@PathVariable Long applicationId) {
        List<ChatMessageResponse> history = chatMessageRepository.findByApplicationIdOrderByTimestampAsc(applicationId)
                .stream().map(msg -> ChatMessageResponse.builder()
                        .id(msg.getId())
                        .applicationId(msg.getApplication().getId())
                        .senderId(msg.getSender().getId())
                        .senderName(msg.getSender().getFirstName() + " " + msg.getSender().getLastName())
                        .senderRole(msg.getSender().getRole().name())
                        .content(msg.getContent())
                        .timestamp(msg.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Historique du chat", history));
    }
}

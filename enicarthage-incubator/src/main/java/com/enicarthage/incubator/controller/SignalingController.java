package com.enicarthage.incubator.controller;

import com.enicarthage.incubator.dto.request.WebRtcSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/signaling")
    public void handleSignaling(@Payload WebRtcSignal signal, Authentication authentication) {
        if (authentication == null) return;
        
        // Broadcast the WebRTC signal (offer/answer/ice-candidate) to the other peer in the room
        messagingTemplate.convertAndSend("/topic/signaling/" + signal.getApplicationId(), signal);
    }
}

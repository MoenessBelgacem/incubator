package com.enicarthage.incubator.repository;

import com.enicarthage.incubator.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByApplicationIdOrderByTimestampAsc(Long applicationId);
}

package com.enicarthage.incubator.repository;

import com.enicarthage.incubator.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    List<EventRegistration> findByEventId(Long eventId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    long countByEventId(Long eventId);

    @Transactional
    @Modifying
    @Query("DELETE FROM EventRegistration er WHERE er.event.id = :eventId")
    void deleteByEventId(Long eventId);
}

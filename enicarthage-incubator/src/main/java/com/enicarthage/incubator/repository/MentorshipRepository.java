package com.enicarthage.incubator.repository;

import com.enicarthage.incubator.model.Mentorship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorshipRepository extends JpaRepository<Mentorship, Long> {
    List<Mentorship> findByMentorId(Long mentorId);
    Optional<Mentorship> findByApplicationId(Long applicationId);
    boolean existsByApplicationIdAndMentorId(Long applicationId, Long mentorId);
}

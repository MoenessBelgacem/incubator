package com.enicarthage.incubator.service;

import com.enicarthage.incubator.model.Application;
import com.enicarthage.incubator.model.Mentorship;
import com.enicarthage.incubator.repository.MentorshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorshipService {
    private final MentorshipRepository mentorshipRepository;

    public List<Application> getIncubatedApplicationsForMentor(Long mentorId) {
        return mentorshipRepository.findByMentorId(mentorId).stream()
                .map(Mentorship::getApplication)
                .toList();
    }

    public Mentorship getMentorshipByApplicationId(Long applicationId) {
        return mentorshipRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Aucun mentor assigné à cette candidature"));
    }

    public Mentorship assignMentor(Long applicationId, Long mentorId, com.enicarthage.incubator.repository.UserRepository userRepository, com.enicarthage.incubator.repository.ApplicationRepository applicationRepository) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable"));
        
        com.enicarthage.incubator.model.User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor introuvable"));
                
        if (mentor.getRole() != com.enicarthage.incubator.model.Role.MENTOR) {
            throw new IllegalArgumentException("L'utilisateur spécifié n'a pas le rôle de MENTOR");
        }
        
        if (mentorshipRepository.existsByApplicationIdAndMentorId(applicationId, mentorId)) {
            throw new IllegalArgumentException("Ce mentor est déjà assigné à cette candidature");
        }

        Mentorship mentorship = Mentorship.builder()
                .application(application)
                .mentor(mentor)
                .build();
                
        return mentorshipRepository.save(mentorship);
    }
}

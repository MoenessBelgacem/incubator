package com.enicarthage.incubator.service;

import com.enicarthage.incubator.model.LandingSection;
import com.enicarthage.incubator.repository.LandingSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LandingSectionService {

    private final LandingSectionRepository repository;
    private final FileStorageService fileStorageService;

    public List<LandingSection> getVisibleSections() {
        return repository.findByVisibleTrueOrderByOrderIndexAsc();
    }

    public List<LandingSection> getAllSections() {
        return repository.findAllByOrderByOrderIndexAsc();
    }

    public LandingSection getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section introuvable"));
    }

    public LandingSection create(LandingSection section, MultipartFile image) {
        if (section.getOrderIndex() == 0) {
            section.setOrderIndex((int) repository.count() + 1);
        }
        if (image != null && !image.isEmpty()) {
            String path = fileStorageService.store(image, "landing");
            section.setImagePath(path);
        }
        return repository.save(section);
    }

    public LandingSection update(Long id, LandingSection updated, MultipartFile image) {
        LandingSection existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setSubtitle(updated.getSubtitle());
        existing.setContent(updated.getContent());
        existing.setBackgroundColor(updated.getBackgroundColor());
        existing.setLayout(updated.getLayout());
        existing.setOrderIndex(updated.getOrderIndex());
        existing.setVisible(updated.isVisible());
        if (image != null && !image.isEmpty()) {
            String path = fileStorageService.store(image, "landing");
            existing.setImagePath(path);
        }
        return repository.save(existing);
    }

    @Transactional
    public void reorder(List<Long> orderedIds) {
        for (int i = 0; i < orderedIds.size(); i++) {
            LandingSection section = getById(orderedIds.get(i));
            section.setOrderIndex(i + 1);
            repository.save(section);
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void initDefaultSections() {
        if (repository.count() > 0) return;

        repository.save(LandingSection.builder()
                .title("The Official Kickoff")
                .subtitle("November 12, 2025")
                .content("Today marks the official kickoff of the ENICARTHAGE Incubator, in the presence of our third- and second-year engineering students. A new chapter begins with this initiative, fully dedicated to supporting our students and researchers in transforming their ideas into real projects and impactful startups.\n\nThe incubator brings together the school's three engineering departments: Computer Engineering, Electrical Engineering, and Industrial Engineering, around one shared vision: Encouraging creativity, innovation, and entrepreneurship at the heart of engineering education.\n\nThrough mentoring, training, and collaboration with business and industry partners, the ENICARTHAGE Incubator aims to create a dynamic ecosystem where ideas grow, talents connect, and innovation thrives. A first partnership agreement was signed with Mine'n Shine to strengthen collaboration with the socio-economic ecosystem.")
                .imagePath("assets/images/slide1.png")
                .backgroundColor("white")
                .layout("text-left-image-right")
                .orderIndex(1)
                .visible(true)
                .build());

        repository.save(LandingSection.builder()
                .title("The First Cohort")
                .subtitle("January 24, 2026")
                .content("It was a real pleasure to conclude the Final Round of the very first incubation session of the ENICARTHAGE Incubator. We were proud to see our finalists successfully complete this final stage after several months of structured evaluation, mentoring, and sustained effort.\n\n🥇 First place: MAAMAR MOHAMED\n🥈 Second place: Moeness BELGACEM\n\nFollowing the jury's deliberation, Mohamed Maamar will receive business and technical support in collaboration with Mine'n Shine. Moeness Belgacem will receive technical support with continued guidance.\n\nLess than three months after the official launch, we already have an active first cohort and a clear path forward.")
                .imagePath("assets/images/slide2.png")
                .backgroundColor("background")
                .layout("image-left-text-right")
                .orderIndex(2)
                .visible(true)
                .build());

        repository.save(LandingSection.builder()
                .title("Jury & Acknowledgments")
                .subtitle("")
                .content("We sincerely thank all jury members for their availability, commitment, valuable insights, and constructive feedback.\n\nFinal Round – January 24, 2026: Imane Channoufi, Mohamed KHADRAOUI, Walid Barhoumi, Houda Ben Attia. Supervisors: Nazeh Ben Ammar, Amor GUEDDANA, Besma Khiari, Monia Bouzid.\n\nScreening Round – December 3, 2025: Nazeh Ben Ammar, Houda Ben Attia, Besma Khiari, Haythem Ghazouani.\n\nIdeation Round – November 19, 2025: Nazeh Ben Ammar, Houda Ben Attia, Monia Bouzid, Amor GUEDDANA.\n\nSpecial thanks to the founders: Besma Khiari, Monia Bouzid, Houda Ben Attia, Imen Kammoun, Khaoula ElBedoui, Walid Barhoumi, Haythem Ghazouani, Iyed Ben Slimen, and Amor GUEDDANA.")
                .imagePath(null)
                .backgroundColor("navy")
                .layout("centered")
                .orderIndex(3)
                .visible(true)
                .build());
    }
}

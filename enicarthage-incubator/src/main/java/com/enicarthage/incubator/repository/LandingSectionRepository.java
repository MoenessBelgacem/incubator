package com.enicarthage.incubator.repository;

import com.enicarthage.incubator.model.LandingSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandingSectionRepository extends JpaRepository<LandingSection, Long> {
    List<LandingSection> findAllByOrderByOrderIndexAsc();
    List<LandingSection> findByVisibleTrueOrderByOrderIndexAsc();
}

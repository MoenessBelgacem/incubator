package com.enicarthage.incubator.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_sections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imagePath;

    @Column(nullable = false)
    @Builder.Default
    private String backgroundColor = "white"; // white, background, navy, gradient

    @Column(nullable = false)
    @Builder.Default
    private String layout = "text-left-image-right"; // text-left-image-right, image-left-text-right, centered, two-columns

    @Column(nullable = false)
    private int orderIndex;

    @Column(nullable = false)
    @Builder.Default
    private boolean visible = true;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

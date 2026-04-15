package com.sthwalo.resumes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Persisted resume draft.
 * The full ResumeData is stored as a JSON string in {@code dataJson}
 * to avoid complex relational mappings for deeply-nested lists.
 * Supports both H2 (dev) and PostgreSQL (prod) via TEXT column type.
 */
@Entity
@Table(name = "resume_drafts")
@Data
public class ResumeDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String title;

    /** Full ResumeData serialized as JSON */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String dataJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

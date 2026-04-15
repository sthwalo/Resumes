package com.sthwalo.resumes.dto;

import java.time.LocalDateTime;

/** Lightweight summary returned by GET /api/drafts (no ResumeData payload) */
public record DraftSummary(
        Long id,
        String ownerName,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

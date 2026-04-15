package com.sthwalo.resumes.dto;

import com.sthwalo.resumes.model.ResumeData;

import java.time.LocalDateTime;

/** Full draft including deserialized ResumeData — returned by GET /api/drafts/{id} */
public record DraftResponse(
        Long id,
        String ownerName,
        String title,
        ResumeData data,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

package com.sthwalo.resumes.dto;

import com.sthwalo.resumes.model.ResumeData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DraftSaveRequest(
        @NotBlank(message = "ownerName is required") String ownerName,
        @NotBlank(message = "title is required")     String title,
        @NotNull(message = "data is required") @Valid ResumeData data
) {}

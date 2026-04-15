package com.sthwalo.resumes.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Top-level API request body for POST /api/resumes.
 *
 * Example:
 * <pre>
 * {
 *   "format": "pdf",
 *   "data": { ... }
 * }
 * </pre>
 */
@Data
public class ResumeRequest {

    @NotBlank(message = "format is required")
    @Pattern(regexp = "html|pdf|docx", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "format must be one of: html, pdf, docx")
    private String format;

    @NotNull(message = "data is required")
    @Valid
    private ResumeData data;
}

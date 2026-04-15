package com.sthwalo.resumes.controller;

import com.sthwalo.resumes.model.ResumeRequest;
import com.sthwalo.resumes.service.ResumeService;
import com.sthwalo.resumes.validator.ValidationResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for resume generation and ATS validation.
 *
 * <pre>
 * POST /api/resumes          — validate + generate (returns file bytes)
 * POST /api/resumes/validate — validate-only (returns JSON report)
 * </pre>
 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Generate a resume file.
     *
     * <p>Request body example:
     * <pre>
     * {
     *   "format": "pdf",
     *   "data": { "personalInfo": {...}, "summary": "...", "experience": [...], ... }
     * }
     * </pre>
     *
     * <p>Responds with the raw file bytes and appropriate Content-Type / Content-Disposition headers.
     * Returns HTTP 422 if ATS ERROR-level rules are violated.
     */
    @PostMapping
    public ResponseEntity<byte[]> generateResume(@Valid @RequestBody ResumeRequest request) throws Exception {
        ResumeService.ExportResult result = resumeService.generateResume(
                request.getData(), request.getFormat());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .header("X-ATS-Score", String.valueOf(result.validation().getAtsScore()))
                .body(result.content());
    }

    /**
     * Validate-only endpoint — returns JSON ATS report without generating a file.
     *
     * <p>Useful for integrations that want to display validation feedback
     * before requesting the actual export.
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateOnly(@Valid @RequestBody ResumeRequest request) {
        ValidationResult result = resumeService.validate(request.getData());
        int status = result.isPassing() ? 200 : 422;
        return ResponseEntity.status(status).body(result);
    }
}

package com.sthwalo.resumes.service;

import com.sthwalo.resumes.exporter.ExporterFactory;
import com.sthwalo.resumes.exporter.ResumeExporter;
import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeService {

    private final List<ResumeValidator> validators;
    private final ExporterFactory exporterFactory;

    public ResumeService(List<ResumeValidator> validators, ExporterFactory exporterFactory) {
        this.validators     = validators;
        this.exporterFactory = exporterFactory;
    }

    /**
     * Run all ATS validators against the resume data.
     *
     * @return ValidationResult — callers can inspect isPassing() and getAtsScore()
     */
    public ValidationResult validate(ResumeData data) {
        ValidationResult result = new ValidationResult();
        for (ResumeValidator validator : validators) {
            validator.validate(data, result);
        }
        return result;
    }

    /**
     * Validate and then export. Throws if any ERROR-level rule fails.
     *
     * @param data   resume content
     * @param format "html" | "pdf" | "docx"
     * @return       ExportResult wrapping the bytes + metadata
     */
    public ExportResult generateResume(ResumeData data, String format) throws Exception {
        ValidationResult validation = validate(data);
        if (!validation.isPassing()) {
            throw new AtsValidationException(validation);
        }
        ResumeExporter exporter = exporterFactory.getExporter(format);
        byte[] bytes = exporter.export(data);
        return new ExportResult(bytes, exporter.getContentType(), exporter.getFilename(data), validation);
    }

    // ── Inner result type ───────────────────────────────────────────────────────

    public record ExportResult(
            byte[] content,
            String contentType,
            String filename,
            ValidationResult validation
    ) {}
}

package com.sthwalo.resumes.validator.rules;

import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.springframework.stereotype.Component;

/**
 * ATS-002 — Professional summary presence.
 *
 * A summary section is the primary target for keyword-matching in
 * Workday, Taleo, and Greenhouse. Without it, ATS keyword scores drop.
 */
@Component
public class SummaryValidator implements ResumeValidator {

    private static final int MIN_WORDS = 30;
    private static final int MAX_WORDS = 100;

    @Override
    public String getRuleId() { return "ATS-002"; }

    @Override
    public void validate(ResumeData data, ValidationResult result) {
        String summary = data.getSummary();
        if (summary == null || summary.isBlank()) {
            result.add(getRuleId(),
                    "Professional summary is missing. Add 2-4 sentences with role keywords to improve ATS ranking.",
                    ValidationSeverity.WARNING);
            return;
        }
        long wordCount = summary.trim().split("\\s+").length;
        if (wordCount < MIN_WORDS) {
            result.add(getRuleId(),
                    "Summary is too short (" + wordCount + " words). Aim for at least " + MIN_WORDS + " words.",
                    ValidationSeverity.WARNING);
        }
        if (wordCount > MAX_WORDS) {
            result.add(getRuleId(),
                    "Summary is too long (" + wordCount + " words). Keep under " + MAX_WORDS + " to avoid ATS truncation.",
                    ValidationSeverity.INFO);
        }
    }
}

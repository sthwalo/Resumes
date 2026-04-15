package com.sthwalo.resumes.validator;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationResult {

    private List<ValidationError> errors = new ArrayList<>();

    public void add(String ruleId, String message, ValidationSeverity severity) {
        errors.add(new ValidationError(ruleId, message, severity));
    }

    public boolean hasErrors() {
        return errors.stream().anyMatch(e -> e.getSeverity() == ValidationSeverity.ERROR);
    }

    public boolean hasWarnings() {
        return errors.stream().anyMatch(e -> e.getSeverity() == ValidationSeverity.WARNING);
    }

    /** true if there are no ERROR-level issues (warnings are allowed) */
    public boolean isPassing() {
        return !hasErrors();
    }

    /**
     * ATS compatibility score 0-100.
     * Each ERROR costs 15 points; each WARNING costs 5 points.
     */
    public int getAtsScore() {
        long errorCount   = errors.stream().filter(e -> e.getSeverity() == ValidationSeverity.ERROR).count();
        long warningCount = errors.stream().filter(e -> e.getSeverity() == ValidationSeverity.WARNING).count();
        return Math.max(0, 100 - (int) (errorCount * 15) - (int) (warningCount * 5));
    }
}

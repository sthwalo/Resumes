package com.sthwalo.resumes.service;

import com.sthwalo.resumes.validator.ValidationResult;

/**
 * Thrown when one or more ATS validation rules at ERROR severity fail.
 * Caught by GlobalExceptionHandler and converted to HTTP 422.
 */
public class AtsValidationException extends RuntimeException {

    private final ValidationResult validationResult;

    public AtsValidationException(ValidationResult validationResult) {
        super("Resume failed ATS validation. Score: " + validationResult.getAtsScore());
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() { return validationResult; }
}

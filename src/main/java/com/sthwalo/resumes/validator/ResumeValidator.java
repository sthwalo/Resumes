package com.sthwalo.resumes.validator;

import com.sthwalo.resumes.model.ResumeData;

/**
 * SPI for ATS validation rules.
 * Each implementation must be a Spring @Component so it is auto-collected
 * by ResumeService via constructor injection of List&lt;ResumeValidator&gt;.
 */
public interface ResumeValidator {

    /** Unique rule ID used in ValidationError — e.g. "ATS-001" */
    String getRuleId();

    /**
     * Examine {@code data} and append any issues to {@code result}.
     * Never throw — always add errors to the result instead.
     */
    void validate(ResumeData data, ValidationResult result);
}

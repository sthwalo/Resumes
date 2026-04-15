package com.sthwalo.resumes.validator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationError {

    /** Rule identifier — e.g. ATS-001 */
    private String ruleId;

    /** Human-readable description of the issue */
    private String message;

    private ValidationSeverity severity;
}

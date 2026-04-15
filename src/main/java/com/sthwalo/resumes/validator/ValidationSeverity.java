package com.sthwalo.resumes.validator;

public enum ValidationSeverity {
    /** Blocks ATS parsing — must be fixed before submission */
    ERROR,
    /** Reduces ATS score — strongly recommended to fix */
    WARNING,
    /** Informational suggestion */
    INFO
}

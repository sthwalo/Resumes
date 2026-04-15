package com.sthwalo.resumes.validator.rules;

import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.springframework.stereotype.Component;

/**
 * ATS-001 — Contact information completeness.
 *
 * Most ATS systems extract contact data first. Missing email or phone
 * causes applicant records to be created without contact details.
 */
@Component
public class ContactInfoValidator implements ResumeValidator {

    @Override
    public String getRuleId() { return "ATS-001"; }

    @Override
    public void validate(ResumeData data, ValidationResult result) {
        var info = data.getPersonalInfo();
        if (info == null) {
            result.add(getRuleId(), "personalInfo section is missing entirely.", ValidationSeverity.ERROR);
            return;
        }
        if (isBlank(info.getEmail())) {
            result.add(getRuleId(), "Email address is missing — required by all ATS.", ValidationSeverity.ERROR);
        }
        if (isBlank(info.getPhone())) {
            result.add(getRuleId(), "Phone number is missing — required by most ATS.", ValidationSeverity.ERROR);
        }
        if (isBlank(info.getFullName())) {
            result.add(getRuleId(), "Full name is missing.", ValidationSeverity.ERROR);
        }
        if (isBlank(info.getLocation())) {
            result.add(getRuleId(), "Location (City, State) is missing — many ATS filter by location.", ValidationSeverity.WARNING);
        }
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}

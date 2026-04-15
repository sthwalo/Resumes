package com.sthwalo.resumes.validator.rules;

import com.sthwalo.resumes.model.Education;
import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * ATS-006 — Education section completeness.
 *
 * Many ATS systems have mandatory degree fields. Missing degree name
 * or institution causes misclassification.
 */
@Component
public class EducationValidator implements ResumeValidator {

    private static final Pattern DATE_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{4}$");

    @Override
    public String getRuleId() { return "ATS-006"; }

    @Override
    public void validate(ResumeData data, ValidationResult result) {
        List<Education> educationList = data.getEducation();
        if (educationList == null || educationList.isEmpty()) {
            result.add(getRuleId(), "Education section is missing.", ValidationSeverity.WARNING);
            return;
        }
        for (int i = 0; i < educationList.size(); i++) {
            Education edu = educationList.get(i);
            String label = "Education[" + i + "]";

            if (isBlank(edu.getDegree())) {
                result.add(getRuleId(), label + ": degree name is missing.", ValidationSeverity.ERROR);
            }
            if (isBlank(edu.getInstitution())) {
                result.add(getRuleId(), label + ": institution name is missing.", ValidationSeverity.ERROR);
            }
            if (!isBlank(edu.getGraduationDate()) && !DATE_PATTERN.matcher(edu.getGraduationDate()).matches()) {
                result.add(getRuleId(),
                        label + ": graduationDate must be MM/YYYY. Got: " + edu.getGraduationDate(),
                        ValidationSeverity.WARNING);
            }
        }
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}

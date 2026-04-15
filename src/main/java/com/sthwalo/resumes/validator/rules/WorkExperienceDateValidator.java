package com.sthwalo.resumes.validator.rules;

import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.model.WorkExperience;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * ATS-003 — Work experience date format and completeness.
 *
 * ATS systems parse dates to compute tenure. Non-standard formats
 * (e.g. "Jan 2020", "2020-01") cause parsing failures in Taleo and SAP SuccessFactors.
 * Required format: MM/YYYY
 */
@Component
public class WorkExperienceDateValidator implements ResumeValidator {

    private static final Pattern DATE_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{4}$");

    @Override
    public String getRuleId() { return "ATS-003"; }

    @Override
    public void validate(ResumeData data, ValidationResult result) {
        List<WorkExperience> experiences = data.getExperience();
        if (experiences == null || experiences.isEmpty()) {
            result.add(getRuleId(), "No work experience entries found.", ValidationSeverity.WARNING);
            return;
        }
        for (int i = 0; i < experiences.size(); i++) {
            WorkExperience exp = experiences.get(i);
            String label = "Experience[" + i + "] — " + exp.getJobTitle() + " at " + exp.getCompany();

            if (exp.getStartDate() == null || !DATE_PATTERN.matcher(exp.getStartDate()).matches()) {
                result.add(getRuleId(),
                        label + ": startDate must be in MM/YYYY format (e.g. 03/2022). Got: " + exp.getStartDate(),
                        ValidationSeverity.ERROR);
            }
            if (!exp.isCurrent()) {
                if (exp.getEndDate() == null || !DATE_PATTERN.matcher(exp.getEndDate()).matches()) {
                    result.add(getRuleId(),
                            label + ": endDate must be in MM/YYYY format or set current=true. Got: " + exp.getEndDate(),
                            ValidationSeverity.ERROR);
                }
            }
            if (exp.getBullets() == null || exp.getBullets().isEmpty()) {
                result.add(getRuleId(),
                        label + ": no achievement bullets. ATS scores drop without keyword-rich bullet points.",
                        ValidationSeverity.WARNING);
            }
        }
    }
}

package com.sthwalo.resumes.validator.rules;

import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.model.Skills;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ATS-004 — Skills section completeness.
 *
 * ATS keyword parsing relies heavily on a dedicated skills section.
 * A plain comma-separated or line-break-separated skills list is the most
 * universally parseable format across Workday, Greenhouse, Lever, and Taleo.
 */
@Component
public class SkillsSectionValidator implements ResumeValidator {

    private static final int MIN_TECHNICAL_SKILLS = 5;

    @Override
    public String getRuleId() { return "ATS-004"; }

    @Override
    public void validate(ResumeData data, ValidationResult result) {
        Skills skills = data.getSkills();
        if (skills == null) {
            result.add(getRuleId(), "Skills section is missing — critical for ATS keyword matching.", ValidationSeverity.ERROR);
            return;
        }

        List<String> technical = skills.getTechnical();
        if (technical == null || technical.isEmpty()) {
            result.add(getRuleId(), "No technical skills listed — ATS will score this resume poorly.", ValidationSeverity.ERROR);
        } else if (technical.size() < MIN_TECHNICAL_SKILLS) {
            result.add(getRuleId(),
                    "Only " + technical.size() + " technical skill(s) listed. Aim for at least " + MIN_TECHNICAL_SKILLS + ".",
                    ValidationSeverity.WARNING);
        } else {
            // Check for skills with special characters that confuse parsers
            List<String> problematic = new ArrayList<>();
            for (String skill : technical) {
                if (skill.contains("|") || skill.contains("/") || skill.contains("\\")) {
                    problematic.add(skill);
                }
            }
            if (!problematic.isEmpty()) {
                result.add(getRuleId(),
                        "These skills contain characters (|, /) that can confuse ATS parsers: " + problematic
                                + ". Use comma-separated entries only.",
                        ValidationSeverity.WARNING);
            }
        }
    }
}

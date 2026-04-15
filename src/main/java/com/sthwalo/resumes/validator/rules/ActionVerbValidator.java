package com.sthwalo.resumes.validator.rules;

import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.model.WorkExperience;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ATS-005 — Action verb usage in bullet points.
 *
 * ATS systems score bullets higher when they start with strong action verbs.
 * This rule checks that each bullet starts with a recognised action verb
 * (not "Responsible for", "Helped", "Worked on" etc.).
 */
@Component
public class ActionVerbValidator implements ResumeValidator {

    private static final List<String> WEAK_PHRASES = List.of(
            "responsible for", "helped", "worked on", "assisted", "involved in",
            "participated in", "contributed to", "tried to", "worked with",
            "helped to", "duties included", "tasks included"
    );

    @Override
    public String getRuleId() { return "ATS-005"; }

    @Override
    public void validate(ResumeData data, ValidationResult result) {
        if (data.getExperience() == null) return;

        for (WorkExperience exp : data.getExperience()) {
            if (exp.getBullets() == null) continue;
            for (String bullet : exp.getBullets()) {
                String lower = bullet.toLowerCase().trim();
                for (String weak : WEAK_PHRASES) {
                    if (lower.startsWith(weak)) {
                        result.add(getRuleId(),
                                "Weak opening phrase detected in bullet: \"" + truncate(bullet, 60)
                                        + "\". Replace with a strong action verb (e.g. Led, Built, Reduced, Delivered).",
                                ValidationSeverity.WARNING);
                        break;
                    }
                }
            }
        }
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}

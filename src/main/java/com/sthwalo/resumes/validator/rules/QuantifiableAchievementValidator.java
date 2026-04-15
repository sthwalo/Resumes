package com.sthwalo.resumes.validator.rules;

import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.model.WorkExperience;
import com.sthwalo.resumes.validator.ResumeValidator;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ATS-007 — Quantifiable achievements in bullets.
 *
 * ATS ranking algorithms and human reviewers score candidates higher when
 * bullets contain metrics (%, $, numbers). This rule checks that at least
 * 40% of all bullets include a numeric value.
 */
@Component
public class QuantifiableAchievementValidator implements ResumeValidator {

    private static final double MIN_METRIC_RATIO = 0.40;
    private static final java.util.regex.Pattern NUMBER_PATTERN =
            java.util.regex.Pattern.compile("\\d+[%$x]?|\\$\\d+|\\d+\\s*(percent|times|million|billion|thousand|k\\b)",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    @Override
    public String getRuleId() { return "ATS-007"; }

    @Override
    public void validate(ResumeData data, ValidationResult result) {
        List<WorkExperience> experiences = data.getExperience();
        if (experiences == null || experiences.isEmpty()) return;

        int totalBullets  = 0;
        int metricBullets = 0;

        for (WorkExperience exp : experiences) {
            if (exp.getBullets() == null) continue;
            for (String bullet : exp.getBullets()) {
                totalBullets++;
                if (NUMBER_PATTERN.matcher(bullet).find()) {
                    metricBullets++;
                }
            }
        }

        if (totalBullets == 0) return;

        double ratio = (double) metricBullets / totalBullets;
        if (ratio < MIN_METRIC_RATIO) {
            int needed = (int) Math.ceil(totalBullets * MIN_METRIC_RATIO) - metricBullets;
            result.add(getRuleId(),
                    "Only " + metricBullets + "/" + totalBullets + " bullets contain metrics. "
                            + "Add numbers/percentages to at least " + needed + " more bullet(s) to reach the 40% threshold.",
                    ValidationSeverity.WARNING);
        }
    }
}

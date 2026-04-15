package com.sthwalo.resumes.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class WorkExperience {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Company name is required")
    private String company;

    /** City, State */
    private String location;

    /** MM/YYYY — required by ATS-003 rule */
    @NotBlank(message = "Start date is required (MM/YYYY)")
    private String startDate;

    /** MM/YYYY — leave null when current = true */
    private String endDate;

    private boolean current;

    /** 3-5 achievement-based bullets, each starting with an action verb */
    private List<String> bullets;
}

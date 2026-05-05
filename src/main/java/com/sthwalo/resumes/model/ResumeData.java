package com.sthwalo.resumes.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Root model for all resume content.
 * Maps 1-to-1 with the JSON body under the "data" key in ResumeRequest.
 */
@Data
public class ResumeData {

    @NotNull(message = "personalInfo is required")
    @Valid
    private PersonalInfo personalInfo;

    /** 2-4 sentence professional summary — improves ATS keyword matching */
    private String summary;

    @JsonAlias({"workExperience"})
    @Valid
    private List<WorkExperience> experience;

    @Valid
    private List<Education> education;

    private Skills skills;

    private List<Certification> certifications;

    private List<Project> projects;
}

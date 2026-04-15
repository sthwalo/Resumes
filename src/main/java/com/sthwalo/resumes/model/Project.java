package com.sthwalo.resumes.model;

import lombok.Data;

import java.util.List;

@Data
public class Project {

    private String name;
    private String description;

    /** Tech stack used — e.g. ["Java", "Spring Boot", "PostgreSQL"] */
    private List<String> technologies;

    /** github.com/user/repo or live URL */
    private String url;

    /** Optional achievement bullets (same format as WorkExperience) */
    private List<String> bullets;
}

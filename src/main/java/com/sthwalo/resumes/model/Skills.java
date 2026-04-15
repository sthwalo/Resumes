package com.sthwalo.resumes.model;

import lombok.Data;

import java.util.List;

@Data
public class Skills {

    /** Hard technical skills — e.g. Java, Python, SQL, REST APIs */
    private List<String> technical;

    /** Developer tools, platforms, IDEs — e.g. Git, Docker, IntelliJ */
    private List<String> tools;

    /** Soft skills — e.g. Leadership, Communication */
    private List<String> soft;

    /** Human languages — e.g. English (Native), Zulu (Fluent) */
    private List<String> languages;
}

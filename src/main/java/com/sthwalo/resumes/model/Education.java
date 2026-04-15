package com.sthwalo.resumes.model;

import lombok.Data;

@Data
public class Education {

    /** Full degree name — e.g. "Bachelor of Science in Computer Science" */
    private String degree;

    private String institution;

    /** City, State */
    private String location;

    /** MM/YYYY */
    private String graduationDate;

    /** e.g. "3.8" — omit if below 3.5 */
    private String gpa;

    /** e.g. "Cum Laude", "Dean's List" */
    private String honors;
}

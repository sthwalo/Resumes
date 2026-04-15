package com.sthwalo.resumes.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonalInfo {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    /** City, State — e.g. "New York, NY". Omit full street address (ATS best practice). */
    private String location;

    /** linkedin.com/in/username (no https:// prefix) */
    private String linkedIn;

    /** github.com/username */
    private String github;

    private String website;
}

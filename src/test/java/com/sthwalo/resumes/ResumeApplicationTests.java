package com.sthwalo.resumes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sthwalo.resumes.model.*;
import com.sthwalo.resumes.service.ResumeService;
import com.sthwalo.resumes.validator.ValidationResult;
import com.sthwalo.resumes.validator.ValidationSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── ATS validation tests ──────────────────────────────────────────────────

    @Test
    void validResume_shouldPassAllRulesWithHighScore() {
        ResumeData data = buildValidResumeData();
        ValidationResult result = resumeService.validate(data);

        assertThat(result.isPassing()).isTrue();
        assertThat(result.getAtsScore()).isGreaterThanOrEqualTo(80);
        assertThat(result.getErrors()).noneMatch(e -> e.getSeverity() == ValidationSeverity.ERROR);
    }

    @Test
    void missingEmail_shouldProduceAts001Error() {
        ResumeData data = buildValidResumeData();
        data.getPersonalInfo().setEmail(null);

        ValidationResult result = resumeService.validate(data);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .anyMatch(e -> "ATS-001".equals(e.getRuleId()) && e.getSeverity() == ValidationSeverity.ERROR);
    }

    @Test
    void invalidDateFormat_shouldProduceAts003Error() {
        ResumeData data = buildValidResumeData();
        data.getExperience().get(0).setStartDate("January 2022"); // wrong format

        ValidationResult result = resumeService.validate(data);

        assertThat(result.getErrors())
                .anyMatch(e -> "ATS-003".equals(e.getRuleId()) && e.getSeverity() == ValidationSeverity.ERROR);
    }

    @Test
    void weakActionVerb_shouldProduceAts005Warning() {
        ResumeData data = buildValidResumeData();
        data.getExperience().get(0).setBullets(List.of("Responsible for managing a team of 5 engineers"));

        ValidationResult result = resumeService.validate(data);

        assertThat(result.getErrors())
                .anyMatch(e -> "ATS-005".equals(e.getRuleId()) && e.getSeverity() == ValidationSeverity.WARNING);
    }

    // ── REST API tests ────────────────────────────────────────────────────────

    @Test
    void postValidResume_html_shouldReturn200WithHtmlContent() throws Exception {
        ResumeRequest request = new ResumeRequest();
        request.setFormat("html");
        request.setData(buildValidResumeData());

        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    void postValidResume_pdf_shouldReturn200WithPdfContent() throws Exception {
        ResumeRequest request = new ResumeRequest();
        request.setFormat("pdf");
        request.setData(buildValidResumeData());

        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/pdf")));
    }

    @Test
    void postValidResume_docx_shouldReturn200WithDocxContent() throws Exception {
        ResumeRequest request = new ResumeRequest();
        request.setFormat("docx");
        request.setData(buildValidResumeData());

        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        org.hamcrest.Matchers.containsString("openxmlformats")));
    }

    @Test
    void missingPersonalInfo_shouldReturn400() throws Exception {
        ResumeRequest request = new ResumeRequest();
        request.setFormat("pdf");
        request.setData(new ResumeData()); // no personalInfo

        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateOnly_validResume_shouldReturn200WithAtsScore() throws Exception {
        ResumeRequest request = new ResumeRequest();
        request.setFormat("pdf");
        request.setData(buildValidResumeData());

        mockMvc.perform(post("/api/resumes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atsScore").exists());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResumeData buildValidResumeData() {
        PersonalInfo info = new PersonalInfo();
        info.setFullName("Jane Doe");
        info.setEmail("jane.doe@example.com");
        info.setPhone("+1-555-123-4567");
        info.setLocation("San Francisco, CA");
        info.setLinkedIn("janedoe");
        info.setGithub("janedoe");

        WorkExperience exp = new WorkExperience();
        exp.setJobTitle("Senior Software Engineer");
        exp.setCompany("Acme Corp");
        exp.setLocation("San Francisco, CA");
        exp.setStartDate("03/2021");
        exp.setCurrent(true);
        exp.setBullets(List.of(
                "Led migration of monolith to microservices, reducing deployment time by 60%",
                "Built real-time data pipeline processing 500k events/day using Kafka and Spring Boot",
                "Mentored 4 junior engineers, improving team velocity by 30%"
        ));

        Education edu = new Education();
        edu.setDegree("Bachelor of Science in Computer Science");
        edu.setInstitution("University of Cape Town");
        edu.setGraduationDate("11/2019");
        edu.setGpa("3.7");

        Skills skills = new Skills();
        skills.setTechnical(List.of("Java", "Spring Boot", "Python", "SQL", "REST APIs", "Kafka", "Docker"));
        skills.setTools(List.of("Git", "IntelliJ IDEA", "Postman", "Jenkins"));
        skills.setSoft(List.of("Leadership", "Communication", "Problem Solving"));

        ResumeData data = new ResumeData();
        data.setPersonalInfo(info);
        data.setSummary(
                "Results-driven software engineer with 5+ years of experience building scalable " +
                "distributed systems in Java and Spring Boot. Proven track record of reducing operational " +
                "costs and improving system reliability. Passionate about clean architecture and developer experience.");
        data.setExperience(List.of(exp));
        data.setEducation(List.of(edu));
        data.setSkills(skills);

        return data;
    }
}

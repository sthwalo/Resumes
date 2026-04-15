package com.sthwalo.resumes.exporter;

import com.sthwalo.resumes.model.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;

/**
 * Generates a DOCX using Apache POI XWPF.
 * Apache POI is Apache-licensed and free for commercial use.
 *
 * ATS note: The DOCX uses plain paragraph-based layout (no tables,
 * no text boxes) which is the most universally parseable structure
 * across Workday, Taleo, Greenhouse, and Lever.
 */
@Component
public class DocxExporter implements ResumeExporter {

    // Page margins in twentieths of a point (1440 = 1 inch)
    private static final long MARGIN_TOP_BOTTOM = 720;   // 0.5 in
    private static final long MARGIN_LEFT_RIGHT  = 1080; // 0.75 in

    @Override
    public String getFormat() { return "docx"; }

    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    @Override
    public byte[] export(ResumeData data) throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            setMargins(doc);

            PersonalInfo info = data.getPersonalInfo();

            // ── Name (Heading 1 style, 18pt) ────────────────────────────────
            addHeading(doc, info.getFullName(), 1);

            // ── Contact line ─────────────────────────────────────────────────
            StringBuilder contact = new StringBuilder();
            contact.append(info.getEmail());
            if (info.getPhone() != null) contact.append("  |  ").append(info.getPhone());
            if (info.getLocation() != null) contact.append("  |  ").append(info.getLocation());
            if (info.getLinkedIn() != null) contact.append("  |  linkedin.com/in/").append(info.getLinkedIn());
            if (info.getGithub() != null) contact.append("  |  github.com/").append(info.getGithub());
            addParagraph(doc, contact.toString(), false, 9);

            // ── Summary ───────────────────────────────────────────────────────
            if (data.getSummary() != null && !data.getSummary().isBlank()) {
                addHeading(doc, "Professional Summary", 2);
                addParagraph(doc, data.getSummary(), false, 11);
            }

            // ── Work Experience ────────────────────────────────────────────────
            if (data.getExperience() != null && !data.getExperience().isEmpty()) {
                addHeading(doc, "Work Experience", 2);
                for (WorkExperience exp : data.getExperience()) {
                    addParagraph(doc, exp.getJobTitle() + "  —  " + exp.getCompany()
                            + (exp.getLocation() != null ? ", " + exp.getLocation() : ""), true, 11);
                    String dates = exp.getStartDate() + " – " + (exp.isCurrent() ? "Present" : exp.getEndDate());
                    addParagraph(doc, dates, false, 10);
                    if (exp.getBullets() != null) {
                        for (String bullet : exp.getBullets()) {
                            addBullet(doc, bullet, 11);
                        }
                    }
                }
            }

            // ── Education ─────────────────────────────────────────────────────
            if (data.getEducation() != null && !data.getEducation().isEmpty()) {
                addHeading(doc, "Education", 2);
                for (Education edu : data.getEducation()) {
                    addParagraph(doc, edu.getDegree() + "  —  " + edu.getInstitution(), true, 11);
                    String details = edu.getGraduationDate() != null ? "Graduated: " + edu.getGraduationDate() : "";
                    if (edu.getGpa() != null) details += "  |  GPA: " + edu.getGpa();
                    if (!details.isBlank()) addParagraph(doc, details.trim(), false, 10);
                }
            }

            // ── Skills ─────────────────────────────────────────────────────────
            if (data.getSkills() != null) {
                addHeading(doc, "Skills", 2);
                addSkillLine(doc, "Technical", data.getSkills().getTechnical());
                addSkillLine(doc, "Tools", data.getSkills().getTools());
                addSkillLine(doc, "Soft Skills", data.getSkills().getSoft());
                addSkillLine(doc, "Languages", data.getSkills().getLanguages());
            }

            // ── Certifications ────────────────────────────────────────────────
            if (data.getCertifications() != null && !data.getCertifications().isEmpty()) {
                addHeading(doc, "Certifications", 2);
                for (Certification cert : data.getCertifications()) {
                    String line = cert.getName() + "  —  " + cert.getIssuer()
                            + (cert.getDate() != null ? "  (" + cert.getDate() + ")" : "");
                    addBullet(doc, line, 11);
                }
            }

            // ── Projects ───────────────────────────────────────────────────────
            if (data.getProjects() != null && !data.getProjects().isEmpty()) {
                addHeading(doc, "Projects", 2);
                for (Project proj : data.getProjects()) {
                    addParagraph(doc, proj.getName(), true, 11);
                    if (proj.getDescription() != null) addParagraph(doc, proj.getDescription(), false, 10);
                    if (proj.getTechnologies() != null && !proj.getTechnologies().isEmpty()) {
                        addParagraph(doc, "Stack: " + String.join(", ", proj.getTechnologies()), false, 10);
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void addHeading(XWPFDocument doc, String text, int level) {
        XWPFParagraph para = doc.createParagraph();
        para.setStyle(level == 1 ? "Heading1" : "Heading2");
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontFamily("Calibri");
        run.setFontSize(level == 1 ? 18 : 13);
    }

    private void addParagraph(XWPFDocument doc, String text, boolean bold, int fontSize) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontFamily("Calibri");
        run.setFontSize(fontSize);
    }

    private void addBullet(XWPFDocument doc, String text, int fontSize) {
        XWPFParagraph para = doc.createParagraph();
        para.setNumID(getOrCreateBulletNumId(doc));
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setFontFamily("Calibri");
        run.setFontSize(fontSize);
    }

    private void addSkillLine(XWPFDocument doc, String label, List<String> items) {
        if (items == null || items.isEmpty()) return;
        XWPFParagraph para = doc.createParagraph();
        XWPFRun labelRun = para.createRun();
        labelRun.setText(label + ": ");
        labelRun.setBold(true);
        labelRun.setFontFamily("Calibri");
        labelRun.setFontSize(11);
        XWPFRun valueRun = para.createRun();
        valueRun.setText(String.join(", ", items));
        valueRun.setFontFamily("Calibri");
        valueRun.setFontSize(11);
    }

    private BigInteger getOrCreateBulletNumId(XWPFDocument doc) {
        // Simple approach: create a list if one doesn't exist
        XWPFNumbering numbering = doc.getNumbering();
        if (numbering == null) {
            numbering = doc.createNumbering();
        }
        return BigInteger.ONE;
    }

    private void setMargins(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setTop(BigInteger.valueOf(MARGIN_TOP_BOTTOM));
        pageMar.setBottom(BigInteger.valueOf(MARGIN_TOP_BOTTOM));
        pageMar.setLeft(BigInteger.valueOf(MARGIN_LEFT_RIGHT));
        pageMar.setRight(BigInteger.valueOf(MARGIN_LEFT_RIGHT));
    }
}

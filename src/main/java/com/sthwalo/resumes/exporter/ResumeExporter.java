package com.sthwalo.resumes.exporter;

import com.sthwalo.resumes.model.ResumeData;

/**
 * Common SPI for all resume output formats.
 * Implementations must be Spring @Component-annotated and injected via
 * ExporterFactory which selects the right one by format name.
 */
public interface ResumeExporter {

    /** Lower-case format identifier: "html", "pdf", "docx" */
    String getFormat();

    /**
     * Render the resume and return the raw bytes of the generated file.
     *
     * @param data     fully validated resume data
     * @return         byte array of the output file
     */
    byte[] export(ResumeData data) throws Exception;

    /** MIME type returned in the HTTP response Content-Type header */
    String getContentType();

    /** Suggested filename for Content-Disposition header */
    default String getFilename(ResumeData data) {
        String name = data.getPersonalInfo().getFullName()
                .toLowerCase().replaceAll("\\s+", "_");
        return name + "_resume." + getFormat();
    }
}

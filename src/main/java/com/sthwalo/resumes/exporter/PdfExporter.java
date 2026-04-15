package com.sthwalo.resumes.exporter;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sthwalo.resumes.model.ResumeData;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * Converts the Thymeleaf HTML output to a PDF using OpenHTMLtoPDF + Apache PDFBox.
 * OpenHTMLtoPDF is Apache-licensed and free for commercial use.
 *
 * ATS note: The resulting PDF is text-based (not image-scanned), ensuring
 * full machine-readability by all ATS systems.
 */
@Component
public class PdfExporter implements ResumeExporter {

    private final HtmlExporter htmlExporter;

    public PdfExporter(HtmlExporter htmlExporter) {
        this.htmlExporter = htmlExporter;
    }

    @Override
    public String getFormat() { return "pdf"; }

    @Override
    public String getContentType() { return "application/pdf"; }

    @Override
    public byte[] export(ResumeData data) throws Exception {
        String html = htmlExporter.render(data);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(out);
        builder.run();

        return out.toByteArray();
    }
}

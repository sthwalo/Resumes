package com.sthwalo.resumes.exporter;

import com.sthwalo.resumes.model.ResumeData;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/**
 * Renders resume as ATS-safe HTML using Thymeleaf.
 * HTML is also the intermediate representation consumed by PdfExporter.
 */
@Component
public class HtmlExporter implements ResumeExporter {

    private final TemplateEngine templateEngine;

    public HtmlExporter(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public String getFormat() { return "html"; }

    @Override
    public String getContentType() { return "text/html; charset=UTF-8"; }

    @Override
    public byte[] export(ResumeData data) {
        String html = render(data);
        return html.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Renders the Thymeleaf template to a String.
     * Re-used by PdfExporter to avoid duplicating template logic.
     */
    public String render(ResumeData data) {
        Context ctx = new Context();
        ctx.setVariable("resume", data);
        return templateEngine.process("resume", ctx);
    }
}

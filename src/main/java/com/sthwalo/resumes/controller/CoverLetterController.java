package com.sthwalo.resumes.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sthwalo.resumes.model.CoverLetterRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/cover-letter")
public class CoverLetterController {

    private final TemplateEngine templateEngine;

    public CoverLetterController(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @PostMapping
    public ResponseEntity<byte[]> generate(@RequestBody CoverLetterRequest request) throws Exception {
        Context ctx = new Context();
        ctx.setVariable("letter", request);
        String html = templateEngine.process("cover-letter", ctx);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(out);
        builder.run();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cover-letter.pdf\"")
                .body(out.toByteArray());
    }
}

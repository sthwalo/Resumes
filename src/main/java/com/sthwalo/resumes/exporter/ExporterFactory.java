package com.sthwalo.resumes.exporter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Selects the correct ResumeExporter by format string.
 * Spring auto-collects all ResumeExporter @Component beans via the constructor.
 */
@Component
public class ExporterFactory {

    private final Map<String, ResumeExporter> exporters;

    public ExporterFactory(List<ResumeExporter> exporterList) {
        this.exporters = exporterList.stream()
                .collect(Collectors.toMap(e -> e.getFormat().toLowerCase(), Function.identity()));
    }

    /**
     * @param format "html", "pdf", or "docx" (case-insensitive)
     * @throws IllegalArgumentException for unknown formats
     */
    public ResumeExporter getExporter(String format) {
        ResumeExporter exporter = exporters.get(format.toLowerCase());
        if (exporter == null) {
            throw new IllegalArgumentException(
                    "Unsupported format: '" + format + "'. Supported: " + exporters.keySet());
        }
        return exporter;
    }
}

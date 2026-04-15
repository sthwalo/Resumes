# Copilot Instructions

## Project Overview
**Resumes** is a Spring Boot 3.3 / Java 17 application that generates ATS-compliant resumes from a structured JSON payload and exports them as **HTML**, **PDF**, or **DOCX** via a REST API.

## Tech Stack
| Concern | Library |
|---|---|
| Web / REST | Spring Boot 3.3.4 (`spring-boot-starter-web`) |
| HTML templates | Thymeleaf (`spring-boot-starter-thymeleaf`) |
| PDF generation | **OpenHTMLtoPDF 1.0.10** + Apache PDFBox (Apache 2.0) |
| DOCX generation | **Apache POI XWPF 5.2.5** (Apache 2.0) |
| Bean validation | `spring-boot-starter-validation` + Jakarta Validation |
| Boilerplate reduction | Lombok |
| Build | Maven (`./mvnw`) |

## Architecture — Four Layers
```
POST /api/resumes
        │
        ▼
ResumeController          ← validates @RequestBody, returns file bytes
        │
        ▼
ResumeService             ← runs all validators, then calls ExporterFactory
        │
   ┌────┴────┐
   ▼         ▼
validators/ exporter/
(rules)   HtmlExporter → PdfExporter (wraps HtmlExporter)
           DocxExporter
```
- **`HtmlExporter`** renders the Thymeleaf template first; **`PdfExporter`** calls `HtmlExporter.render()` — never duplicate template logic.
- **`ExporterFactory`** auto-collects all `ResumeExporter @Component` beans. Adding a new format = new class + `@Component`, no factory edits needed.
- **`ResumeValidator` rules** (`validator/rules/`) are Spring `@Component`s auto-collected by `ResumeService` via `List<ResumeValidator>` injection.

## ATS Rules (validator/rules/)
| Rule ID | Class | Checks |
|---|---|---|
| ATS-001 | `ContactInfoValidator` | Email, phone, full name present |
| ATS-002 | `SummaryValidator` | Summary present, 30-100 words |
| ATS-003 | `WorkExperienceDateValidator` | Dates in MM/YYYY, bullets present |
| ATS-004 | `SkillsSectionValidator` | ≥5 technical skills, no `|` / `/` separators |
| ATS-005 | `ActionVerbValidator` | Bullets don't start with weak phrases |
| ATS-006 | `EducationValidator` | Degree + institution present, date MM/YYYY |
| ATS-007 | `QuantifiableAchievementValidator` | ≥40% of bullets contain a number/% |

ATS score = `max(0, 100 − errors×15 − warnings×5)`. HTTP 422 is returned if any ERROR fires.

## API
```
POST /api/resumes           → file bytes (html/pdf/docx); X-ATS-Score header
POST /api/resumes/validate  → JSON ValidationResult (atsScore + errors list)
```
Request body: `{ "format": "pdf|html|docx", "data": { ... } }` — see `src/main/resources/schema/resume.schema.json`.

## Key Conventions
- **Dates always MM/YYYY** — any other format triggers ATS-003/ATS-006 errors.
- **Contact info lives in `<body>`**, never in `<header>`/`<footer>` tags (ATS parsers skip those).
- **No tables or text boxes in DOCX** — POI exporter uses plain paragraphs + bullet lists only.
- **`ValidationError` carries `ruleId`, `message`, `severity`** (ERROR / WARNING / INFO). Never throw from a validator — always append to `ValidationResult`.
- **`ResumeData`** is the root model; `ResumeRequest` wraps it with `format`.

## Developer Workflows
```bash
# First-time setup (requires JDK 17+)
./mvnw wrapper:wrapper   # generates mvnw if missing

# Build + run all tests
./mvnw clean verify

# Run locally (http://localhost:8080)
./mvnw spring-boot:run

# Quick smoke-test (generate a PDF)
curl -X POST http://localhost:8080/api/resumes \
  -H "Content-Type: application/json" \
  -d @src/test/resources/sample-resume.json \
  --output out.pdf
```

## Key Files
| Path | Purpose |
|---|---|
| `pom.xml` | All dependencies and versions |
| `src/main/resources/schema/resume.schema.json` | Canonical input schema |
| `src/main/resources/templates/resume.html` | Thymeleaf template (shared by HTML + PDF) |
| `src/main/java/.../validator/rules/` | All 7 ATS rule classes |
| `src/main/java/.../exporter/` | HtmlExporter, PdfExporter, DocxExporter, ExporterFactory |
| `src/test/java/.../ResumeApplicationTests.java` | Integration + validator unit tests |

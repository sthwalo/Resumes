package com.sthwalo.resumes.controller;

import com.sthwalo.resumes.dto.DraftResponse;
import com.sthwalo.resumes.dto.DraftSaveRequest;
import com.sthwalo.resumes.dto.DraftSummary;
import com.sthwalo.resumes.service.ResumeDraftService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD API for resume drafts stored in the database.
 *
 * <pre>
 * GET    /api/drafts              — list all (summaries, newest first)
 * GET    /api/drafts?owner=name   — filter by owner name
 * POST   /api/drafts              — create new draft
 * GET    /api/drafts/{id}         — load full draft (with ResumeData)
 * PUT    /api/drafts/{id}         — update draft
 * DELETE /api/drafts/{id}         — delete draft
 * </pre>
 */
@RestController
@RequestMapping("/api/drafts")
public class DraftController {

    private final ResumeDraftService draftService;

    public DraftController(ResumeDraftService draftService) {
        this.draftService = draftService;
    }

    @GetMapping
    public List<DraftSummary> list(@RequestParam(required = false) String owner) {
        if (owner != null && !owner.isBlank()) {
            return draftService.listByOwner(owner);
        }
        return draftService.listAll();
    }

    @PostMapping
    public ResponseEntity<DraftResponse> create(@Valid @RequestBody DraftSaveRequest req) {
        DraftResponse saved = draftService.create(req);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/{id}")
    public DraftResponse getById(@PathVariable Long id) {
        return draftService.getById(id);
    }

    @PutMapping("/{id}")
    public DraftResponse update(@PathVariable Long id,
                                @Valid @RequestBody DraftSaveRequest req) {
        return draftService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        draftService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

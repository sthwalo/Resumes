package com.sthwalo.resumes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sthwalo.resumes.dto.DraftResponse;
import com.sthwalo.resumes.dto.DraftSaveRequest;
import com.sthwalo.resumes.dto.DraftSummary;
import com.sthwalo.resumes.entity.ResumeDraft;
import com.sthwalo.resumes.model.ResumeData;
import com.sthwalo.resumes.repository.ResumeDraftRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResumeDraftService {

    private final ResumeDraftRepository repo;
    private final ObjectMapper objectMapper;

    public ResumeDraftService(ResumeDraftRepository repo, ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    /** Create a new draft */
    public DraftResponse create(DraftSaveRequest req) {
        ResumeDraft draft = new ResumeDraft();
        draft.setOwnerName(req.ownerName());
        draft.setTitle(req.title());
        draft.setDataJson(serialize(req.data()));
        return toResponse(repo.save(draft));
    }

    /** Update an existing draft */
    public DraftResponse update(Long id, DraftSaveRequest req) {
        ResumeDraft draft = findOrThrow(id);
        draft.setOwnerName(req.ownerName());
        draft.setTitle(req.title());
        draft.setDataJson(serialize(req.data()));
        return toResponse(repo.save(draft));
    }

    /** List all drafts (summary only, newest first) */
    @Transactional(readOnly = true)
    public List<DraftSummary> listAll() {
        return repo.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /** List drafts belonging to a specific owner */
    @Transactional(readOnly = true)
    public List<DraftSummary> listByOwner(String ownerName) {
        return repo.findByOwnerNameIgnoreCaseOrderByUpdatedAtDesc(ownerName).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /** Get full draft by ID */
    @Transactional(readOnly = true)
    public DraftResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /** Delete a draft */
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Draft not found: " + id);
        }
        repo.deleteById(id);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ResumeDraft findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Draft not found: " + id));
    }

    private String serialize(ResumeData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ResumeData", e);
        }
    }

    private ResumeData deserialize(String json) {
        try {
            return objectMapper.readValue(json, ResumeData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ResumeData", e);
        }
    }

    private DraftResponse toResponse(ResumeDraft d) {
        return new DraftResponse(d.getId(), d.getOwnerName(), d.getTitle(),
                deserialize(d.getDataJson()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private DraftSummary toSummary(ResumeDraft d) {
        return new DraftSummary(d.getId(), d.getOwnerName(), d.getTitle(),
                d.getCreatedAt(), d.getUpdatedAt());
    }
}

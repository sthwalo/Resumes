package com.sthwalo.resumes.repository;

import com.sthwalo.resumes.entity.ResumeDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeDraftRepository extends JpaRepository<ResumeDraft, Long> {

    /** All drafts newest-first, used for the "My Drafts" panel */
    List<ResumeDraft> findAllByOrderByUpdatedAtDesc();

    /** Filter by owner name (case-insensitive) */
    List<ResumeDraft> findByOwnerNameIgnoreCaseOrderByUpdatedAtDesc(String ownerName);
}

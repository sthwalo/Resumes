/* ── State ─────────────────────────────────────────────────────────────── */
let currentDraftId = null;

/* ── Initialise ────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  initTagInput('tag-technical');
  initTagInput('tag-tools');
  initTagInput('tag-soft');
  initTagInput('tag-languages');
  addExperience();          // start with one empty experience slot
  initWordCount();
  initAutoValidate();
});

/* ═══════════════════════════════════════════════════════════════════════
   TAG INPUT
═══════════════════════════════════════════════════════════════════════ */
function initTagInput(wrapperId) {
  const wrapper = document.getElementById(wrapperId);
  if (!wrapper) return;

  const input = wrapper.querySelector('.tag-input');
  if (!input) return;

  input.addEventListener('keydown', e => {
    if ((e.key === 'Enter' || e.key === ',') && input.value.trim()) {
      e.preventDefault();
      addTag(wrapperId, input.value.trim().replace(/,$/, ''));
      input.value = '';
    }
    // backspace on empty input removes last tag
    if (e.key === 'Backspace' && input.value === '') {
      const tags = wrapper.querySelectorAll('.tag');
      if (tags.length) tags[tags.length - 1].remove();
    }
  });

  // click anywhere in wrapper focuses the input
  wrapper.addEventListener('click', () => input.focus());
}

function addTag(wrapperId, value) {
  if (!value) return;
  const wrapper = document.getElementById(wrapperId);
  if (!wrapper) return;

  // deduplicate (case-insensitive)
  const existing = getTagValues(wrapperId).map(t => t.toLowerCase());
  if (existing.includes(value.toLowerCase())) return;

  const input = wrapper.querySelector('.tag-input');
  const tag = document.createElement('span');
  tag.className = 'tag';
  tag.dataset.value = value;
  tag.innerHTML = `${_esc(value)}<button type="button" class="tag-remove" title="Remove">&#215;</button>`;
  tag.querySelector('.tag-remove').addEventListener('click', () => tag.remove());
  wrapper.insertBefore(tag, input);
}

function getTagValues(wrapperId) {
  const wrapper = document.getElementById(wrapperId);
  if (!wrapper) return [];
  return [...wrapper.querySelectorAll('.tag')].map(t => t.dataset.value);
}

function setTagValues(wrapperId, values = []) {
  const wrapper = document.getElementById(wrapperId);
  if (!wrapper) return;
  // clear existing tags
  wrapper.querySelectorAll('.tag').forEach(t => t.remove());
  values.forEach(v => addTag(wrapperId, v));
}

/* ═══════════════════════════════════════════════════════════════════════
   WORD COUNT (summary textarea)
═══════════════════════════════════════════════════════════════════════ */
function initWordCount() {
  const ta = document.querySelector('textarea[name="summary"]');
  const el = document.getElementById('word-count');
  if (!ta || !el) return;
  const update = () => {
    const words = ta.value.trim() ? ta.value.trim().split(/\s+/).length : 0;
    el.textContent = `${words} word${words !== 1 ? 's' : ''} (30–100 recommended)`;
    el.className = 'word-count ' + (words >= 30 && words <= 100 ? 'ok' : words > 0 ? 'warn' : '');
  };
  ta.addEventListener('input', update);
  update();
}

/* ═══════════════════════════════════════════════════════════════════════
   AUTO-VALIDATE (debounced, on any input change)
═══════════════════════════════════════════════════════════════════════ */
function initAutoValidate() {
  let timer;
  document.getElementById('main-form').addEventListener('input', () => {
    clearTimeout(timer);
    timer = setTimeout(() => runValidation(), 1200);
  });
}

async function runValidation() {
  const data = collectResumeData();
  try {
    const res = await fetch('/api/resumes/validate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ format: 'html', data })
    });
    const result = await res.json();
    updateAtsScore(result.atsScore ?? null);
    renderValidationPanel(result);
  } catch (_) { /* silent — server may not be up yet */ }
}

/* ═══════════════════════════════════════════════════════════════════════
   COLLECT FORM DATA
═══════════════════════════════════════════════════════════════════════ */
function collectFormData() {
  return {
    ownerName: val('owner-name'),
    title:     val('draft-title') || 'Untitled Resume',
    data:      collectResumeData()
  };
}

function collectResumeData() {
  return {
    personalInfo: {
      fullName:  val('pi-fullName'),
      email:     val('pi-email'),
      phone:     val('pi-phone'),
      location:  val('pi-location'),
      linkedIn:  val('pi-linkedIn'),
      github:    val('pi-github'),
      website:   val('pi-website')
    },
    summary: val('pi-summary'),
    experience: collectEntries('exp-list', readExperience),
    education:      collectEntries('edu-list',  readEducation),
    skills: {
      technical: getTagValues('tag-technical'),
      tools:     getTagValues('tag-tools'),
      soft:      getTagValues('tag-soft'),
      languages: getTagValues('tag-languages')
    },
    certifications: collectEntries('cert-list', readCertification),
    projects:       collectEntries('proj-list', readProject)
  };
}

function collectEntries(listId, reader) {
  const list = document.getElementById(listId);
  if (!list) return [];
  return [...list.querySelectorAll('.entry-card')].map(reader).filter(Boolean);
}

function readExperience(card) {
  return {
    jobTitle:  cardVal(card, 'jobTitle'),
    company:   cardVal(card, 'company'),
    location:  cardVal(card, 'location'),
    startDate: cardVal(card, 'startDate'),
    endDate:   card.querySelector('[name="current"]')?.checked ? null : cardVal(card, 'endDate'),
    current:   card.querySelector('[name="current"]')?.checked ?? false,
    bullets:   readBullets(card)
  };
}

function readEducation(card) {
  return {
    degree:         cardVal(card, 'degree'),
    institution:    cardVal(card, 'institution'),
    location:       cardVal(card, 'location'),
    graduationDate: cardVal(card, 'graduationDate'),
    gpa:            cardVal(card, 'gpa'),
    honors:         cardVal(card, 'honors')
  };
}

function readCertification(card) {
  return {
    name:       cardVal(card, 'name'),
    issuer:     cardVal(card, 'issuer'),
    date:       cardVal(card, 'date'),
    expiryDate: cardVal(card, 'expiryDate')
  };
}

function readProject(card) {
  return {
    name:         cardVal(card, 'name'),
    description:  cardVal(card, 'description'),
    technologies: cardVal(card, 'technologies').split(',').map(s => s.trim()).filter(Boolean),
    url:          cardVal(card, 'url'),
    bullets:      readBullets(card)
  };
}

function readBullets(card) {
  return [...card.querySelectorAll('.bullet-row input')]
    .map(i => i.value.trim()).filter(Boolean);
}

/* ═══════════════════════════════════════════════════════════════════════
   POPULATE FORM (load a draft into the UI)
═══════════════════════════════════════════════════════════════════════ */
function populateForm(draft) {
  set('owner-name',  draft.ownerName ?? '');
  set('draft-title', draft.title     ?? '');

  const d = draft.data ?? {};
  const pi = d.personalInfo ?? {};
  set('pi-fullName', pi.fullName ?? '');
  set('pi-email',    pi.email    ?? '');
  set('pi-phone',    pi.phone    ?? '');
  set('pi-location', pi.location ?? '');
  set('pi-linkedIn', pi.linkedIn ?? '');
  set('pi-github',   pi.github   ?? '');
  set('pi-website',  pi.website  ?? '');
  set('pi-summary',  d.summary   ?? '');
  document.querySelector('textarea[name="summary"]') &&
    document.querySelector('textarea[name="summary"]').dispatchEvent(new Event('input'));

  // Experience
  const expList = document.getElementById('exp-list');
  expList.innerHTML = '';
  (d.experience ?? []).forEach(e => {
    expList.appendChild(buildExperienceCard(e));
  });
  if (!expList.children.length) addExperience();

  // Education
  const eduList = document.getElementById('edu-list');
  eduList.innerHTML = '';
  (d.education ?? []).forEach(e => {
    eduList.appendChild(buildEducationCard(e));
  });

  // Skills
  setTagValues('tag-technical', d.skills?.technical ?? []);
  setTagValues('tag-tools',     d.skills?.tools     ?? []);
  setTagValues('tag-soft',      d.skills?.soft      ?? []);
  setTagValues('tag-languages', d.skills?.languages ?? []);

  // Certifications
  const certList = document.getElementById('cert-list');
  certList.innerHTML = '';
  (d.certifications ?? []).forEach(c => {
    certList.appendChild(buildCertCard(c));
  });

  // Projects
  const projList = document.getElementById('proj-list');
  projList.innerHTML = '';
  (d.projects ?? []).forEach(p => {
    projList.appendChild(buildProjectCard(p));
  });
}

/* ═══════════════════════════════════════════════════════════════════════
   ADD ENTRY HELPERS (called by buttons in index.html)
═══════════════════════════════════════════════════════════════════════ */
function addExperience() {
  document.getElementById('exp-list').appendChild(buildExperienceCard({}));
}

function addEducation() {
  document.getElementById('edu-list').appendChild(buildEducationCard({}));
}

function addCertification() {
  document.getElementById('cert-list').appendChild(buildCertCard({}));
}

function addProject() {
  document.getElementById('proj-list').appendChild(buildProjectCard({}));
}

/* ── Entry card builders ────────────────────────────────────────────────── */
function buildExperienceCard(e = {}) {
  const card = document.createElement('div');
  card.className = 'entry-card';
  card.innerHTML = `
    <div class="entry-hd">
      <span class="entry-label">Work Experience</span>
      <button type="button" class="btn-remove" onclick="this.closest('.entry-card').remove()">&#8722; Remove</button>
    </div>
    <div class="grid-2" style="margin-bottom:10px">
      <div class="fg"><label>Job Title *</label><input name="jobTitle"  type="text" value="${_esc(e.jobTitle  ?? '')}" placeholder="e.g. Driver"/></div>
      <div class="fg"><label>Company *</label>  <input name="company"   type="text" value="${_esc(e.company   ?? '')}" placeholder="e.g. Rapid Freight Solutions"/></div>
      <div class="fg"><label>Location</label>   <input name="location"  type="text" value="${_esc(e.location  ?? '')}" placeholder="e.g. Johannesburg, GP"/></div>
      <div class="fg"><label>Start Date * <small>(MM/YYYY)</small></label><input name="startDate" type="text" value="${_esc(e.startDate ?? '')}" placeholder="01/2020"/></div>
    </div>
    <div class="grid-2" style="margin-bottom:10px">
      <div class="fg"><label>End Date <small>(MM/YYYY)</small></label><input name="endDate" type="text" value="${_esc(e.current ? '' : (e.endDate ?? ''))}" placeholder="12/2023" ${e.current ? 'disabled' : ''}/></div>
      <div class="fg" style="justify-content:center">
        <label class="check-label" style="margin-top:22px">
          <input type="checkbox" name="current" ${e.current ? 'checked' : ''}
            onchange="this.closest('.entry-card').querySelector('[name=endDate]').disabled=this.checked"/>
          Currently working here
        </label>
      </div>
    </div>
    <div class="bullets-section">
      <div style="font-size:13px;font-weight:600;margin-bottom:6px">Key Achievements / Responsibilities</div>
      <div class="bullets-list">${(e.bullets ?? []).map(b => bulletRowHtml(b)).join('')}</div>
      <button type="button" class="btn-add-small" onclick="addBullet(this.previousElementSibling)">+ Add bullet point</button>
    </div>`;
  if (!(e.bullets ?? []).length) {
    addBullet(card.querySelector('.bullets-list'));
  }
  return card;
}

function buildEducationCard(e = {}) {
  const card = document.createElement('div');
  card.className = 'entry-card';
  card.innerHTML = `
    <div class="entry-hd">
      <span class="entry-label">Education</span>
      <button type="button" class="btn-remove" onclick="this.closest('.entry-card').remove()">&#8722; Remove</button>
    </div>
    <div class="grid-2">
      <div class="fg"><label>Degree / Qualification *</label><input name="degree"         type="text" value="${_esc(e.degree         ?? '')}" placeholder="e.g. Bachelor of Science"/></div>
      <div class="fg"><label>Institution *</label>           <input name="institution"    type="text" value="${_esc(e.institution    ?? '')}" placeholder="e.g. UNISA"/></div>
      <div class="fg"><label>Location</label>                <input name="location"       type="text" value="${_esc(e.location       ?? '')}" placeholder="e.g. Pretoria"/></div>
      <div class="fg"><label>Graduation Date <small>(MM/YYYY)</small></label><input name="graduationDate" type="text" value="${_esc(e.graduationDate ?? '')}" placeholder="06/2020"/></div>
      <div class="fg"><label>GPA / Grade</label>             <input name="gpa"            type="text" value="${_esc(e.gpa            ?? '')}" placeholder="optional"/></div>
      <div class="fg"><label>Honours / Distinctions</label>  <input name="honors"         type="text" value="${_esc(e.honors         ?? '')}" placeholder="optional"/></div>
    </div>`;
  return card;
}

function buildCertCard(c = {}) {
  const card = document.createElement('div');
  card.className = 'entry-card';
  card.innerHTML = `
    <div class="entry-hd">
      <span class="entry-label">Certification</span>
      <button type="button" class="btn-remove" onclick="this.closest('.entry-card').remove()">&#8722; Remove</button>
    </div>
    <div class="grid-2">
      <div class="fg"><label>Certification Name *</label><input name="name"       type="text" value="${_esc(c.name       ?? '')}" placeholder="e.g. PDP Licence"/></div>
      <div class="fg"><label>Issuer</label>              <input name="issuer"     type="text" value="${_esc(c.issuer     ?? '')}" placeholder="e.g. DLTC"/></div>
      <div class="fg"><label>Date <small>(MM/YYYY)</small></label><input name="date"  type="text" value="${_esc(c.date       ?? '')}" placeholder="03/2022"/></div>
      <div class="fg"><label>Expiry <small>(MM/YYYY)</small></label><input name="expiryDate" type="text" value="${_esc(c.expiryDate ?? '')}" placeholder="03/2025 (optional)"/></div>
    </div>`;
  return card;
}

function buildProjectCard(p = {}) {
  const card = document.createElement('div');
  card.className = 'entry-card';
  card.innerHTML = `
    <div class="entry-hd">
      <span class="entry-label">Project</span>
      <button type="button" class="btn-remove" onclick="this.closest('.entry-card').remove()">&#8722; Remove</button>
    </div>
    <div class="grid-2" style="margin-bottom:10px">
      <div class="fg"><label>Project Name *</label><input name="name"         type="text" value="${_esc(p.name        ?? '')}" placeholder="e.g. Route Optimisation"/></div>
      <div class="fg"><label>URL</label>            <input name="url"          type="url"  value="${_esc(p.url         ?? '')}" placeholder="https://..."/></div>
      <div class="fg" style="grid-column:1/-1"><label>Description</label><textarea name="description" rows="2" placeholder="Brief description...">${_esc(p.description ?? '')}</textarea></div>
      <div class="fg" style="grid-column:1/-1"><label>Technologies <small>(comma-separated)</small></label><input name="technologies" type="text" value="${_esc((p.technologies ?? []).join(', '))}" placeholder="Excel, GPS Tracking, Google Maps"/></div>
    </div>
    <div class="bullets-section">
      <div style="font-size:13px;font-weight:600;margin-bottom:6px">Highlights</div>
      <div class="bullets-list">${(p.bullets ?? []).map(b => bulletRowHtml(b)).join('')}</div>
      <button type="button" class="btn-add-small" onclick="addBullet(this.previousElementSibling)">+ Add bullet point</button>
    </div>`;
  return card;
}

/* ── Bullet helpers ──────────────────────────────────────────────────────── */
function bulletRowHtml(value = '') {
  return `<div class="bullet-row">
    <span class="bullet-dot">&#8226;</span>
    <input type="text" placeholder="Start with a strong action verb…" value="${_esc(value)}"/>
    <button type="button" class="btn-rm-bullet" onclick="this.closest('.bullet-row').remove()" title="Remove">&#215;</button>
  </div>`;
}

function addBullet(bulletsListEl) {
  bulletsListEl.insertAdjacentHTML('beforeend', bulletRowHtml(''));
  bulletsListEl.lastElementChild.querySelector('input').focus();
}

/* ═══════════════════════════════════════════════════════════════════════
   SAVE DRAFT
═══════════════════════════════════════════════════════════════════════ */
async function handleSave() {
  const owner = val('owner-name').trim();
  if (!owner) { showToast('Please enter your name before saving.', 'warn'); return; }

  const payload = collectFormData();
  try {
    let res;
    if (currentDraftId) {
      res = await fetch(`/api/drafts/${currentDraftId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    } else {
      res = await fetch('/api/drafts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    }
    if (!res.ok) throw new Error(await res.text());
    const draft = await res.json();
    currentDraftId = draft.id;
    document.getElementById('draft-badge').textContent = `ID #${draft.id}`;
    showToast('Draft saved successfully!', 'success');
    runValidation();
  } catch (err) {
    showToast('Save failed: ' + err.message, 'error');
  }
}

/* ═══════════════════════════════════════════════════════════════════════
   GENERATE (PDF / DOCX / HTML)
═══════════════════════════════════════════════════════════════════════ */
async function handleGenerate(format) {
  const data = collectResumeData();
  const btn = document.querySelector(`.btn-gen[data-fmt="${format}"]`) ||
              document.querySelector(`.btn-gen-outline[data-fmt="${format}"]`);

  if (btn) { btn.disabled = true; btn.textContent = 'Generating…'; }
  try {
    const res = await fetch('/api/resumes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ format, data })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      const issues = err.errors?.map(e => e.message).join('\n') ?? err.message;
      showToast('Cannot generate: ' + issues, 'error');
      return;
    }

    const score = res.headers.get('X-ATS-Score');
    if (score !== null) updateAtsScore(parseInt(score, 10));

    const blob     = await res.blob();
    const ext      = format.toLowerCase();
    const owner    = val('owner-name').replace(/\s+/g, '_') || 'resume';
    const filename = `${owner}_resume.${ext}`;
    const url      = URL.createObjectURL(blob);
    const a        = document.createElement('a');
    a.href         = url;
    a.download     = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 10000);

    showToast(`${format.toUpperCase()} downloaded! ATS ${score ?? '?'}/100`, 'success');
  } catch (err) {
    showToast('Error: ' + err.message, 'error');
  } finally {
    if (btn) { btn.disabled = false; btn.dataset.fmt === 'pdf' ? btn.textContent = '⬇ PDF'
                                                                : btn.dataset.fmt === 'docx'
                                                                  ? btn.textContent = '⬇ DOCX'
                                                                  : btn.textContent = '⬇ HTML'; }
  }
}

/* ═══════════════════════════════════════════════════════════════════════
   DRAFTS PANEL
═══════════════════════════════════════════════════════════════════════ */
async function openDraftsPanel() {
  document.getElementById('drafts-overlay').style.display = 'flex';
  const list = document.getElementById('drafts-list');
  list.innerHTML = '<p class="loading-text">Loading…</p>';

  try {
    const res     = await fetch('/api/drafts');
    const drafts  = await res.json();
    if (!drafts.length) {
      list.innerHTML = '<p class="draft-empty">No saved drafts yet.<br>Fill in the form and click Save.</p>';
      return;
    }
    list.innerHTML = '';
    drafts.forEach(d => {
      const card = document.createElement('div');
      card.className = 'draft-card';
      card.innerHTML = `
        <div class="draft-info">
          <strong>${_esc(d.title)}</strong>
          <span class="draft-owner">${_esc(d.ownerName)}</span>
          <span class="draft-date">${fmtDate(d.updatedAt)}</span>
        </div>
        <div class="draft-actions">
          <button type="button" class="btn-sm-primary" onclick="loadDraft(${d.id})">Open</button>
          <button type="button" class="btn-sm-danger"  onclick="deleteDraft(${d.id}, this)">Delete</button>
        </div>`;
      list.appendChild(card);
    });
  } catch (err) {
    list.innerHTML = `<p class="draft-empty">Could not load drafts.<br>${err.message}</p>`;
  }
}

async function loadDraft(id) {
  try {
    const res   = await fetch(`/api/drafts/${id}`);
    if (!res.ok) throw new Error('Draft not found');
    const draft = await res.json();
    populateForm(draft);
    currentDraftId = draft.id;
    document.getElementById('draft-badge').textContent = `ID #${draft.id}`;
    document.getElementById('drafts-overlay').style.display = 'none';
    showToast('Draft loaded!', 'info');
    runValidation();
  } catch (err) {
    showToast('Load failed: ' + err.message, 'error');
  }
}

async function deleteDraft(id, btn) {
  if (!confirm('Delete this draft? This cannot be undone.')) return;
  try {
    const res = await fetch(`/api/drafts/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Delete failed');
    btn.closest('.draft-card').remove();
    if (currentDraftId === id) {
      currentDraftId = null;
      document.getElementById('draft-badge').textContent = '';
    }
    showToast('Draft deleted.', 'info');
  } catch (err) {
    showToast('Delete failed: ' + err.message, 'error');
  }
}

/* ═══════════════════════════════════════════════════════════════════════
   NEW RESUME
═══════════════════════════════════════════════════════════════════════ */
function newResume() {
  if (!confirm('Start a new resume? Unsaved changes will be lost.')) return;
  document.getElementById('main-form').reset();
  document.getElementById('exp-list').innerHTML  = '';
  document.getElementById('edu-list').innerHTML  = '';
  document.getElementById('cert-list').innerHTML = '';
  document.getElementById('proj-list').innerHTML = '';
  ['tag-technical','tag-tools','tag-soft','tag-languages'].forEach(id => {
    document.getElementById(id).querySelectorAll('.tag').forEach(t => t.remove());
  });
  currentDraftId = null;
  document.getElementById('draft-badge').textContent = '';
  updateAtsScore(null);
  clearValidationPanel();
  addExperience();
  showToast('New resume started.', 'info');
}

/* ═══════════════════════════════════════════════════════════════════════
   ATS SCORE DISPLAY
═══════════════════════════════════════════════════════════════════════ */
function updateAtsScore(score) {
  const el = document.getElementById('ats-score');
  if (!el) return;
  if (score === null || score === undefined) {
    el.textContent = '—';
    el.className = 'ats-value neutral';
    return;
  }
  el.textContent = score;
  el.className = 'ats-value ' + (score >= 90 ? 'excellent'
                               : score >= 70 ? 'good'
                               : score >= 50 ? 'fair'
                               :               'poor');
}

/* ═══════════════════════════════════════════════════════════════════════
   VALIDATION PANEL
═══════════════════════════════════════════════════════════════════════ */
function renderValidationPanel(result) {
  let panel = document.getElementById('val-panel');
  if (!panel) {
    panel = document.createElement('div');
    panel.id = 'val-panel';
    panel.className = 'validation-panel';
    const form = document.getElementById('main-form');
    form.insertBefore(panel, form.firstChild);
  }

  const errors = result.errors ?? [];
  if (!errors.length) {
    panel.innerHTML = '<div class="val-item val-success">&#10003; ATS validation passed — no issues found</div>';
    return;
  }

  panel.innerHTML = errors.map(e => {
    const cls = e.severity === 'ERROR' ? 'val-error'
              : e.severity === 'WARNING' ? 'val-warning' : 'val-info';
    const icon = e.severity === 'ERROR' ? '&#10005;' : e.severity === 'WARNING' ? '&#9888;' : 'ℹ';
    return `<div class="val-item ${cls}">${icon} [${e.ruleId}] ${_esc(e.message)}</div>`;
  }).join('');
}

function clearValidationPanel() {
  const panel = document.getElementById('val-panel');
  if (panel) panel.remove();
}

/* ═══════════════════════════════════════════════════════════════════════
   TOAST
═══════════════════════════════════════════════════════════════════════ */
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity .3s';
    setTimeout(() => toast.remove(), 350);
  }, 3200);
}

/* ═══════════════════════════════════════════════════════════════════════
   UTILITIES
═══════════════════════════════════════════════════════════════════════ */
function val(id)            { return (document.getElementById(id)?.value ?? '').trim(); }
function set(id, v)         { const el = document.getElementById(id); if (el) el.value = v; }
function cardVal(card, name){ return (card.querySelector(`[name="${name}"]`)?.value ?? '').trim(); }
function _esc(str)          { return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
function fmtDate(iso)       {
  if (!iso) return '';
  try { return new Date(iso).toLocaleDateString('en-ZA', { day:'numeric', month:'short', year:'numeric' }); }
  catch { return iso; }
}

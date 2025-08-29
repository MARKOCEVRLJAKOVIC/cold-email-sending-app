// src/ui/components/CampaignWizardModal.js
import { createCampaign } from '../../api/campaignsApi.js';
import { http } from '../../api/http.js';
import { qs } from '../../utils/dom.js';

let state = {
  step: 1,
  name: '',
  accounts: [],
  leadsFile: null,
  sequence: {
    subject: '',
    body: '',
    followups: []
  }
};

export function openCampaignWizard() {
  renderModal();
}

function renderModal() {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.innerHTML = `<div class="modal"><div id="wizard-content"></div></div>`;
  document.body.appendChild(overlay);

  renderStep();

  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) closeModal();
  });
}

function closeModal() {
  const overlay = qs('.modal-overlay');
  if (overlay) overlay.remove();
  state = { step: 1, name:'', accounts:[], leadsFile:null, sequence:{subject:'',body:'',followups:[]} };
}

function renderStep() {
  const container = qs('#wizard-content');
  if (!container) return;

  switch (state.step) {
    case 1: renderStep1(container); break;
    case 2: renderStep2(container); break;
    case 3: renderStep3(container); break;
    case 4: renderStep4(container); break;
    case 5: renderStep5(container); break;
  }
}

// ========== STEP 1 ==========
function renderStep1(el) {
  el.innerHTML = `
    <div class="wizard-header">
      <span class="step-label">Step</span>
      <span class="step-number">1/5</span>
    </div>
    <div class="progress-bar">
      <div class="progress" style="width: 20%;"></div>
    </div>
    <h2>Name Your Campaign</h2>
    <input type="text" id="campaignName" placeholder="Type campaign name here" value="${state.name}">
    <div class="actions">
      <button class="btn primary" id="nextBtn">Next Step</button>
    </div>
  `;

  qs('#nextBtn').addEventListener('click', () => {
    const name = qs('#campaignName').value.trim();
    if (!name) {
      alert("Campaign name is required.");
      return;
    }
    state.name = name;
    state.step++;
    renderStep();
  });
}

// ========== STEP 2 ========== 
async function renderStep2(el) {
  el.innerHTML = `
    <div class="wizard-header">
      <span class="step-label">Step</span>
      <span class="step-number">2/5</span>
    </div>
    <div class="progress-bar">
      <div class="progress" style="width: 40%;"></div>
    </div>

    <h2>Select Gmail Accounts</h2>
    <div id="accountsList">Loading accounts...</div>
    <label><input type="checkbox" id="selectAll"> Select All</label>
    <div class="actions">
      <button class="btn primary" id="nextBtn">Next Step</button>
    </div>
  `;

  const accountsList = qs('#accountsList');

  try {
    const gmail = await http.get('/gmail-smtp').then(r => r.data);

    const gmailAccounts = gmail.filter(a => a.enabled).map(a => ({ type: 'gmail', ...a }));

    if (!gmailAccounts.length) {
      accountsList.innerHTML = `<p>No enabled Gmail accounts connected.</p>`;
      return;
    }

    accountsList.innerHTML = gmailAccounts.map(acc => `
      <label>
        <input type="checkbox" class="acc" value="${acc.type}:${acc.id}" checked>
        ${acc.email} <small>(${acc.type})</small>
      </label><br>
    `).join('');

  } catch (err) {
    console.error(err);
    accountsList.innerHTML = `<p class="error">Failed to load Gmail accounts: ${err.error}</p>`;
  }

  document.querySelector('#selectAll').addEventListener('change', e => {
    document.querySelectorAll('.acc').forEach(cb => cb.checked = e.target.checked);
  });

  document.querySelector('#nextBtn').addEventListener('click', () => {
    const selected = Array.from(document.querySelectorAll('.acc:checked')).map(cb => cb.value);
    if (!selected.length) return alert("Select at least one Gmail account");
    state.accounts = selected;
    state.step++;
    renderStep();
  });
}


// ========== STEP 3 ==========
function renderStep3(el) {
  el.innerHTML = `
    <div class="wizard-header">
      <span class="step-label">Step</span>
      <span class="step-number">3/5</span>
    </div>
    <div class="progress-bar">
      <div class="progress" style="width: 60%;"></div>
    </div>

    <h2>Import Your Leads From <strong>.csv</strong> File</h2>

    <div class="dropzone" id="dropzone">
      <div class="dropzone-content">
        <div class="dropzone-icon"><i class="fa-solid fa-cloud-arrow-up"></i></div>
        <p>Drag & Drop File Here</p>
        <span>or</span>
        <button class="btn" id="uploadBtn">Upload File</button>
      </div>
    </div>

    <input type="file" id="fileInput" accept=".csv" style="display:none">

    

    <div class="actions">
      <button class="btn primary" id="nextBtn">Next Step</button>
    </div>
  `;

  const fileInput = qs('#fileInput');
  const dropzone = qs('#dropzone');
  const uploadBtn = qs('#uploadBtn');

  uploadBtn.addEventListener('click', () => fileInput.click());

  dropzone.addEventListener('dragover', e => {
    e.preventDefault();
    dropzone.classList.add('dragover');
  });

  dropzone.addEventListener('dragleave', () => dropzone.classList.remove('dragover'));

  dropzone.addEventListener('drop', e => {
    e.preventDefault();
    dropzone.classList.remove('dragover');
    handleFile(e.dataTransfer.files[0]);
  });

  fileInput.addEventListener('change', () => handleFile(fileInput.files[0]));

  function handleFile(file) {
    if (!file) return;
    if (!file.name.endsWith('.csv')) {
      alert("Only .csv files are allowed");
      return;
    }
    state.leadsFile = file;
    dropzone.innerHTML = `
      <div class="dropzone-content">
        <strong>File selected:</strong> ${file.name}
      </div>
    `;
  }

  qs('#nextBtn').addEventListener('click', () => {
    if (!state.leadsFile) {
      alert("Please upload a .csv file");
      return;
    }
    state.step++;
    renderStep();
  });
}
// ========== STEP 4 ==========
function renderStep4(el) {
  el.innerHTML = `
   <div class="wizard-header">
      <span class="step-label">Step</span>
      <span class="step-number">4/5</span>
    </div>
    <div class="progress-bar">
      <div class="progress" style="width: 80%;"></div>
    </div>
    </div>

    <h2>Create Your Sequence</h2>

    <div class="sequence-editor">
      <div class="email-block">
        <h3>New Email</h3>
        <input type="text" id="subject" placeholder="Subject.." value="${state.sequence.subject}">
        <textarea id="body" placeholder="Write your email..." class="email-textarea">${state.sequence.body}</textarea>
      </div>

      <div class="followup-block">
        <label>
          Follow up after 
          <input type="number" id="fDays" value="4" min="1" style="width:60px"> days 
          <small>(If no reply)</small>
        </label>
        <textarea id="fBody" placeholder="Write your follow-up email..." class="email-textarea"></textarea>
      </div>
    </div>

    <div class="actions">
      <button class="btn primary" id="nextBtn">Next Step</button>
    </div>
  `;

  // dodaj event
  qs('#nextBtn').addEventListener('click', () => {
    const subject = qs('#subject').value.trim();
    const body = qs('#body').value.trim();

    if (!subject || !body) {
      alert("Subject and body are required");
      return;
    }

    state.sequence.subject = subject;
    state.sequence.body = body;

    const fBody = qs('#fBody').value.trim();
    if (fBody) {
      state.sequence.followups.push({
        body: fBody,
        days: parseInt(qs('#fDays').value)
      });
    }

    state.step++;
    renderStep();
  });
}


// ========== STEP 5 ==========
function renderStep5(el) {
  el.innerHTML = `
    <h2>Step 5/5: Finish Campaign</h2>
    <label>Schedule sending (optional):</label>
    <input type="datetime-local" id="scheduledAt">
    <div class="actions">
      <button class="btn primary" id="launchBtn">Launch a campaign</button>
    </div>
  `;

  qs('#launchBtn').addEventListener('click', async () => {
    try {
      const scheduledAtInput = qs('#scheduledAt').value;
      // Spring traži LocalDateTime (ISO format), npr. "2025-08-24T17:00:00"
      const scheduledAt = scheduledAtInput 
        ? scheduledAtInput.replace('T', 'T') + ":00" // osiguravamo sekunde
        : new Date().toISOString().slice(0, 19);     // ako nije setovan, odmah sada

      // 1️⃣ Kreiraj kampanju
      const campaign = await createCampaign({
        name: state.name,
        accounts: state.accounts,
        leadsFile: state.leadsFile ? state.leadsFile.name : null
      });

      // 2️⃣ Kreiraj template
      const template = await http.post(`/templates`, {
        name: 'Wizard Template',
        subject: state.sequence.subject,
        message: state.sequence.body,
        campaignId: campaign.id
      });
      state.templateId = template.data.id;

      // 3️⃣ Kreiraj follow-up ako ih ima
      for (let f of state.sequence.followups) {
        await http.post(`/follow-ups/campaign/${campaign.id}`, {
          delayDays: f.days,
          message: f.body,
          templateOrder: 1
        });
      }

      // 4️⃣ Pripremi FormData za send-batch
      const formData = new FormData();
      formData.append('file', state.leadsFile);
      formData.append('scheduledAt', scheduledAt);
      formData.append('templateId', state.templateId);
      formData.append('campaignId', campaign.id);

      state.accounts.forEach(acc => {
        const id = acc.split(':')[1]; // format type:id
        formData.append('smtpId', id);
      });

      // 5️⃣ Pošalji batch (bitno: Authorization mora biti velikim slovom)
      await http.post('/email-messages/send-batch', formData, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      closeModal();
      location.reload();

    } catch (err) {
      console.error(err);
      alert("Error creating campaign or sending emails. Check console for details.");
    }
  });
}

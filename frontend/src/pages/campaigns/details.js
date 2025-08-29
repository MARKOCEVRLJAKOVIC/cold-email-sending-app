// src/pages/campaigns/details.js

import { mount, toast, confirmDialog, qs } from '../../utils/dom.js';
import * as campaignsApi from '../../api/campaignsApi.js';
import { openCampaignWizard } from '../../ui/components/CampaignWizardModal.js';
import * as emailsApi from '../../api/emailsApi.js';


const CampaignDetails = {
  async render(root, ctx) {
    const id = ctx.params.id;
    mount(root, `
      <div class="campaign-card center">
        <div class="spinner"></div>
        <div class="mt-2">Loading campaign…</div>
      </div>
    `);

    try {
      const campaign = await campaignsApi.getCampaign(id);
      const stats = await campaignsApi.getCampaignStats(id);

      const messages = await emailsApi.listByCampaign(id);
      

      mount(root, `
  <div class="container">
    <div class="campaign-card">
      <h2 class="mb-2"><i class="fa-solid fa-lightbulb"></i> ${campaign.name}</h2>
      <p class="helper mb-2">${campaign.description || 'No description'}</p>
      <div class="helper">Created: ${new Date(campaign.createdAt).toLocaleString()}</div>

      <hr class="sep mt-2 mb-2" />

      <h3 class="mb-2">Stats</h3>
      <div class="kpis">
        <div class="stat-card total">
          <div class="stat-value">${stats.total}</div>
          <div class="stat-label">Total</div>
        </div>
        <div class="stat-card sent">
          <div class="stat-value">${stats.sent}</div>
          <div class="stat-label">Sent</div>
        </div>
        <div class="stat-card replied">
          <div class="stat-value">${stats.replied}</div>
          <div class="stat-label">Replied</div>
        </div>
        <div class="stat-card failed">
          <div class="stat-value">${stats.failed}</div>
          <div class="stat-label">Failed</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-value">${stats.pending}</div>
          <div class="stat-label">Pending</div>
        </div>
      </div>

      <div class="actions mt-3">
        <button id="updateBtn" class="btn secondary"><i class="fa-solid fa-pen"></i> Update</button>
        <button id="deleteBtn" class="btn danger"><i class="fa-solid fa-trash"></i> Delete</button>
        <a href="/campaigns/${id}/stats" class="btn"><i class="fa-solid fa-chart-pie"></i> Full Stats</a>
      </div>

      <hr class="sep mt-3 mb-2" />

      <h3 class="mb-2">Messages</h3>
<div class="messages-list">
  ${messages.length === 0 
    ? '<p class="helper">No messages yet.</p>'
    : messages.map(m => `
      <div class="message-card flex-between">
        <div class="message-main">
          <div><strong>Recipient:</strong> ${m.recipientName || ''} &lt;${m.recipientEmail}&gt;</div>
          <div><strong>Message:</strong> ${m.sentMessage || '(no content)'}</div>
        </div>
        <div class="message-meta">
          <div>
            <span class="message-status ${m.status ? m.status.toLowerCase() : 'pending'}">
              ${m.status || 'PENDING'}
            </span>
          </div>
          <div class="helper">
            ${m.sentAt 
              ? `Sent at: ${new Date(m.sentAt).toLocaleString()}`
              : m.scheduledAt 
                ? `Scheduled for: ${new Date(m.scheduledAt).toLocaleString()}`
                : 'Not sent'}
          </div>
        </div>
      </div>
    `).join('')}
</div>


`);


      // akcije
      qs('#updateBtn', root).addEventListener('click', (e) => {
        e.preventDefault();
        openCampaignWizard(campaign);
      });

      qs('#deleteBtn', root).addEventListener('click', async (e) => {
        e.preventDefault();
        if (await confirmDialog('Delete this campaign?')) {
          try {
            await campaignsApi.deleteCampaign(id);
            toast('Campaign deleted', 'success');
            window.location.href = '/campaigns';
          } catch (err) {
            toast(err.error || 'Delete failed', 'error');
          }
        }
      });

    } catch (e) {
      mount(root, `<div class="campaign-card error">⚠️ Error loading campaign: ${e.error || 'Unknown error'}</div>`);
    }
  }
};

export default CampaignDetails;

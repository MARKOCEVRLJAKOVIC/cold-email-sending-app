import { mount } from '../../utils/dom.js';
import * as campaignsApi from '../../api/campaignsApi.js';

const CampaignStats = {
  async render(root, ctx) {
    const id = ctx.params.id;
    mount(root, `
      <div class="campaign-card center">
        <div class="spinner"></div>
        <div class="mt-2">Loading statistics…</div>
      </div>
    `);

    try {
      const stats = await campaignsApi.getCampaignStats(id);

      mount(root, `
        <div class="container">
          <div class="campaign-card">
            <h2 class="mb-2"><i class="fa-solid fa-chart-pie"></i>  Campaign Statistics</h2>
            <p class="helper mb-3">Overview of emails for campaign #${id}</p>

            <div class="kpis">
              <div class="stat-card total">
                <div class="stat-value">${stats.total}</div>
                <div class="stat-label">Total Emails</div>
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

            <hr class="sep mt-3 mb-3" />

            <div class="actions">
              <a href="/campaigns" class="btn secondary"><i class="fa-solid fa-arrow-left"></i> Back to list</a>
            </div>
          </div>
        </div>
      `);

    } catch (e) {
      mount(root, `
        <div class="campaign-card error">
          ⚠️ Error loading stats: ${e.error || 'Unknown error'}
        </div>
      `);
    }
  }
};

export default CampaignStats;

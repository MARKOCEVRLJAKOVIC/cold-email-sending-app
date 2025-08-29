// /src/pages/campaigns/create.js
import { mount, qs } from '../../utils/dom.js';
import { openCampaignWizard } from '../../ui/components/CampaignWizardModal.js';

export default {
  render(root) {
    mount(root, `
      <div class="card" style="padding:1rem">
        <h2>Create Campaign</h2>
        <p>Use the wizard to create a campaign step by step.</p>
        <button class="btn primary" id="openWizardBtn">Open Wizard</button>
      </div>
    `);

    qs('#openWizardBtn').addEventListener('click', () => {
      openCampaignWizard();
    });
  }
};

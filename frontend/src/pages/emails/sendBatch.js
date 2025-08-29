import { validateForm } from '../../utils/forms.js';
import { mount, toast } from '../../utils/dom.js';
import * as emailsApi from '../../api/emailsApi.js';
import * as templatesApi from '../../api/templatesApi.js';
import * as smtpApi from '../../api/smtpApi.js';
import * as campaignsApi from '../../api/campaignsApi.js';

const SendBatch = {
  async render(root){
    // Preload selects (no caching to keep it simple here)
    const [templates, smtpList, campaigns] = await Promise.all([
      templatesApi.listTemplates().catch(()=>[]),
      smtpApi.listSmtp().catch(()=>[]),
      campaignsApi.listCampaigns().catch(()=>[]),
    ]);

    mount(root, `
      <div class="card" style="padding:1rem; max-width:760px; margin: 0 auto">
        <h2>Send Batch Emails</h2>
        <form id="sbForm" enctype="multipart/form-data">
          <label class="mt-2">
            <div class="helper">CSV File (email,firstName,lastName...)</div>
            <input name="file" type="file" accept=".csv" />
          </label>

          <label class="mt-2">
            <div class="helper">Scheduled At (ISO date/time)</div>
            <input name="scheduledAt" type="datetime-local" />
          </label>

          <label class="mt-2">
            <div class="helper">Template</div>
            <select name="templateId">
              ${templates.map(t=>`<option value="${t.id}">${t.name}</option>`).join('')}
            </select>
          </label>

          <label class="mt-2">
            <div class="helper">SMTP Accounts</div>
            <select name="smtpIds" multiple size="6">
              ${smtpList.map(s=>`<option value="${s.id}">${s.email} (${s.smtpType})</option>`).join('')}
            </select>
          </label>

          <label class="mt-2">
            <div class="helper">Campaign</div>
            <select name="campaignId">
              ${campaigns.map(c=>`<option value="${c.id}">${c.name}</option>`).join('')}
            </select>
          </label>

          <div class="row mt-2">
            <button class="btn" type="submit">Upload & Schedule</button>
            <a class="btn ghost" href="/emails">Cancel</a>
          </div>
        </form>
      </div>
    `);

    const form = root.querySelector('#sbForm');
    form.addEventListener('submit', async (e)=>{
      e.preventDefault();
      try{
        // Yup validation
        const yup = await import('https://cdn.jsdelivr.net/npm/yup@1.4.0/+esm');
        const values = await validateForm(form, {
          scheduledAt: yup.string().required('Pick schedule'),
          templateId: yup.number().transform(v=>Number(v)).required(),
          campaignId: yup.number().transform(v=>Number(v)).required(),
          // file & smtpIds handled manually
        });

        const file = form.querySelector('input[name="file"]').files[0];
        if (!file){ throw { inner:[{path:'file', message:'CSV file is required'}] }; }
        const smtpIds = Array.from(form.querySelector('[name="smtpIds"]').selectedOptions).map(o=>Number(o.value));

        await emailsApi.sendBatch({
          ...values,
          file,
          smtpIds
        });

        toast('Batch scheduled', 'success');
        page.redirect('/emails');
      }catch(err){
        if (!err.inner) toast(err.error || 'Failed to schedule', 'error');
      }
    });
  }
};

export default SendBatch;

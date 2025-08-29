// src/pages/smtp/list.js
import { mount, qs, toast } from '../../utils/dom.js';
import { http } from '../../api/http.js';
import { getOauthUrl, listGmailSmtp, deleteSmtp } from '../../api/gmailApi.js';


async function fetchGmailAccounts() {
  const { data } = await http.get('/gmail-smtp');
  return data;
}

function renderTable(title, rows) {
  return `
    <div class="card" style="padding:1rem; margin-top:1rem">
      <h3>${title}</h3>
      <table class="table">
        <thead>
          <tr><th>Email</th><th>Actions</th></tr>
        </thead>
        <tbody>
          ${
            rows.length
              ? rows.map(r => `
                <tr>
                  <td>${r.email}</td>
                  <td>
                    <button class="btn danger" data-del="${r.id}"><i class="fa-solid fa-trash"></i> Delete</button>
                  </td>
                </tr>
              `).join('')
              : `<tr><td colspan="2">No Gmail accounts connected</td></tr>`
          }
        </tbody>
      </table>
        <button class="btn primary" id="connectGmail" style="margin-top:1rem;">
          <i class="fa-brands fa-google" style="margin-right:0px;"></i>
          Connect Google Email
        </button>
      </div>
  `;
}

export default {
  async render(root) {
    try {
      let gmail = await fetchGmailAccounts();

      // 👇 filtriramo samo enabled
      // gmail = gmail.filter(acc => acc.enabled);

      gmail = (gmail || []).filter(acc => acc.enabled);
      console.log('Gmail accounts:', gmail);    


      gmail = gmail.filter(acc => acc.enabled);


      mount(root, `
        ${renderTable('Gmail Accounts', gmail)}
      `);

      // Handle delete
      root.querySelectorAll('[data-del]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.del;
        try {
          await deleteSmtp(id); // koristi API funkciju
          toast('Account deleted', 'success');
          this.render(root); // reload liste
        } catch (err) {
            console.error(err);
            toast('Failed to delete', 'error');
            }
          });
        });

      // Handle connect Gmail
      const connectBtn = qs('#connectGmail', root);
      if (connectBtn) {
        connectBtn.addEventListener('click', async () => {
          const { data } = await http.get('/oauth-url');
          if (!data?.url) {
            toast('Failed to get OAuth URL', 'error');
            return;
          }
          window.location.href = data.url;
        });
      }

    } catch (err) {
      mount(root, `<div class="card" style="padding:1rem">Error: ${err.message}</div>`);
    }
  }
};

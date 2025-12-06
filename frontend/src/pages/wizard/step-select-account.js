// src/pages/wizard/step-select-account.js
import { getAllSmtp } from '../../api/smtpApi.js';
import { getAllGmail } from '../../api/gmailApi.js';
import { mount, qs } from '../../utils/dom.js';

export default {
  async render(root, onSelect) {
    try {
      // povuci obe liste paralelno
      const [smtp, gmail] = await Promise.all([
        getAllSmtp(),
        getAllGmail()
      ]);

      // spoji i filtriraj samo enabled
      const accounts = [
        ...smtp.filter(a => a.enabled).map(a => ({ ...a, type: 'SMTP' })),
        ...gmail.filter(a => a.enabled).map(a => ({ ...a, type: 'GMAIL' }))
      ];

      // render
      mount(
        root,
        `
          <h2>Select Sending Account</h2>
          <div class="card" style="padding:1rem; margin-top:1rem">
            ${
              accounts.length
                ? `
                  <table class="table">
                    <thead>
                      <tr><th>Email</th><th>Type</th><th>Action</th></tr>
                    </thead>
                    <tbody>
                      ${accounts
                        .map(
                          acc => `
                          <tr>
                            <td>${acc.email}</td>
                            <td>${acc.type}</td>
                            <td><button class="btn primary" data-id="${acc.id}" data-type="${acc.type}">Select</button></td>
                          </tr>
                        `
                        )
                        .join('')}
                    </tbody>
                  </table>
                `
                : `<p>No active accounts. Please connect Gmail or add SMTP first.</p>`
            }
          </div>
        `
      );

      // handle select click
      root.querySelectorAll('[data-id]').forEach(btn => {
        btn.addEventListener('click', () => {
          const id = btn.dataset.id;
          const type = btn.dataset.type;
          const selected = accounts.find(a => a.id == id && a.type === type);
          if (onSelect) onSelect(selected);
        });
      });
    } catch (err) {
      mount(root, `<div class="card" style="padding:1rem">Error: ${err.message}</div>`);
    }
  }
};
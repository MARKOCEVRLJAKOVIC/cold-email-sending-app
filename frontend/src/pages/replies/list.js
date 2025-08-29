import { mount } from '../../utils/dom.js';
import { listReplies } from '../../api/repliesApi.js';

export default {
  async render(root) {
    mount(root, `
      <div class="campaign-card center" style="padding:2rem">
        <div class="spinner"></div>
        <div class="mt-2">Loading replies…</div>
      </div>
    `);

    try {
      const replies = await listReplies();

      if (!replies || replies.length === 0) {
        mount(root, `
          <div class="campaign-card p-4 center">
            <p class="helper">No replies yet.</p>
            <a href="/campaigns" class="btn mt-2">Go to Campaigns</a>
          </div>
        `);
        return;
      }

      mount(root, `
        <div class="container">
          <div class="campaign-card p-4">
            <h2 class="mb-3"><i class="fa-solid fa-envelope"></i> Replies</h2>
            <table class="table">
              <thead>
                <tr>
                  <th>Sender</th>
                  <th>Subject</th>
                  <th>Received</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                ${replies.map(r => `
                  <tr>
                    <td>${r.senderEmail}</td>
                    <td>${r.subject || '(no subject)'}</td>
                    <td>${new Date(r.receivedAt).toLocaleString()}</td>
                    <td>
                      <a href="/replies/${r.id}" class="btn small"><i class="fa-solid fa-expand"></i> View</a>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `);

    } catch (e) {
      mount(root, `
        <div class="campaign-card error">
          ⚠️ Error loading replies: ${e.error || 'Unknown error'}
        </div>
      `);
    }
  }
};

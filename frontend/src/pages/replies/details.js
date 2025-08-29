// src/pages/replies/details.js

import { mount, qs, toast } from '../../utils/dom.js';
import { getReply, respondReply } from '../../api/repliesApi.js';

export default {
  async render(root, ctx) {
    const id = ctx.params.id;

    mount(root, `
      <div class="campaign-card center" style="padding:2rem">
        <div class="spinner"></div>
        <div class="mt-2">Loading reply…</div>
      </div>
    `);

    try {
      const reply = await getReply(id);

      mount(root, `
        <div class="container">
          <div class="campaign-card p-4">
          
            <h2 class="mb-2">
              <i class="fa-solid fa-envelope"></i> Reply from ${reply.senderEmail}
            </h2>
            <p class="helper"><strong>Subject:</strong> ${reply.subject}</p>
            <p class="helper"><strong>Received:</strong> ${new Date(reply.receivedAt).toLocaleString()}</p>

            <hr class="sep mt-2 mb-2" />

            <div class="mb-2">
              <h3>Original message</h3>
              <p class="helper">${reply.content || 'No previous message available'}</p>
            </div>

            <hr class="sep mt-2 mb-2" />

            <div>
              <textarea 
                id="replyMessage" 
                rows="5" 
                placeholder="Type your reply here..." 
                style="width:100%; margin-bottom:1rem">
              </textarea>
              <div class="actions">
                <a href="/replies" class="btn ghost">
                  <i class="fa-solid fa-arrow-left"></i> Back
                </a>
                <button id="sendReplyBtn" class="btn">
                  <i class="fa-solid fa-reply"></i> Send Reply
                </button>
              </div>
            </div>
          </div>
        </div>
      `);

      // ✅ sada šaljemo samo { message }
      qs('#sendReplyBtn', root).addEventListener('click', async () => {
        const message = qs('#replyMessage', root).value.trim();
        if (!message) return toast('Message is empty', 'error');

        try {
          await respondReply(reply.id, message);
          toast('Reply sent!', 'success');
          window.location.href = '/replies';
        } catch (e) {
          toast(e.error || 'Failed to send reply', 'error');
        }
      });

    } catch (e) {
      mount(root, `<div class="campaign-card error">⚠️ Error loading reply: ${e.error || 'Unknown error'}</div>`);
    }
  }
};

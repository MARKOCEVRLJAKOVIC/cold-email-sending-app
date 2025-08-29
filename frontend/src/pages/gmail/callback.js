import { mount, toast } from '../../utils/dom.js';
import { oauthCallback } from '../../api/gmailApi.js';

export default {
  async render(root) {
    mount(root, `
      <div class="card" style="padding:1rem">
        <h2>Connecting Gmail...</h2>
        <div class="spinner"></div>
      </div>
    `);

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    if (!code) {
      mount(root, `
        <div class="card" style="padding:1rem">
          <p>Error: Missing OAuth code</p>
        </div>
      `);
      return;
    }

    try {
      // pozovi backend /callback sa code
      const smtpDto = await oauthCallback({ code });

      console.log("✅ Gmail connected, backend response:", smtpDto);

      toast('Gmail account connected!', 'success');
      window.location.href = '/smtp'; // redirect na listu smtp naloga
    } catch (err) {
      console.error("❌ OAuth callback error:", err);

      mount(root, `
        <div class="card" style="padding:1rem">
          <p>Error connecting Gmail: ${err.error || err.message || 'Unknown error'}</p>
        </div>
      `);
    }
  }
};

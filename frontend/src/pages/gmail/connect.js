import { mount } from '../../utils/dom.js';
import { getOauthUrl } from '../../api/gmailApi.js';

export default {
  async render(root) {
    mount(root, `
      <div class="card" style="padding:1rem">
        <h2>Connect Gmail</h2>
        <button id="connectBtn" class="btn btn-primary">Connect with Google</button>
      </div>
    `);

    document.querySelector('#connectBtn').addEventListener('click', async () => {
      const { url } = await getOauthUrl();
      window.location.href = url; // otvori Google OAuth
    });
  }
};

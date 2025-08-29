// src/ui/layout.js

import { authStore } from '../store/authStore.js';
import { openCampaignWizard } from './components/CampaignWizardModal.js';
import { qs } from '../utils/dom.js';`
`
import page from 'page';

export function mountLayout(root){
  root.innerHTML = `
    <div class="app-shell">
      <header class="header">
      <div class="brand">
        <img src="/assets/logo.png" alt="SendBox Logo" class="logo">
        <span>SendBox</span>
    </div>
      <div class="row">
          <span id="user-chip" class="badge">Guest</span>
          <button class="btn secondary theme-toggle" id="themeToggle" title="Toggle theme">Theme</button>
          <button class="btn ghost" id="logoutBtn" title="Logout">Logout</button>
        </div>
      </header>
      <div class="layout">
        <aside class="sidebar" id="sidebar">
        <button class="btn primary" id="createCampaignSidebarBtn" style="margin-bottom:1rem;">
          + Create Campaign
        </button>
          <nav class="nav">
            <a href="/campaigns" data-link><span><i class="fa-solid fa-lightbulb"></i></span><span class="label">Campaigns</span></a>
            <a href="/smtp" data-link><span><i class="fa-solid fa-user"></i></span><span class="label">Sending Accounts</span></a>
            <a href="/replies" data-link><span><i class="fa-solid fa-inbox"></i></span><span class="label">Master Inbox</span></a>
            <a href="/campaigns" data-link><span><i class="fa-solid fa-chart-pie"></i></span><span class="label">Statistics</span></a>

          </nav>
        </aside>
        <main class="main">
          <div class="container" id="main-outlet">
            <!-- pages mount -->
          
          </div>
        </main>
      </div>
    </div>
    
  `;

  // sidebar active state
  const nav = qs('#sidebar');
  nav.addEventListener('click', (e) => {
    const a = e.target.closest('a[data-link]');
    if (a){ nav.querySelectorAll('a').forEach(x=>x.classList.remove('active')); a.classList.add('active'); }
  });

  // theme
  qs('#themeToggle').addEventListener('click', () => {
    const rootEl = document.documentElement;
    rootEl.setAttribute('data-theme', rootEl.getAttribute('data-theme')==='dark' ? 'light' : 'dark');
  });

  // logout
  qs('#logoutBtn').addEventListener('click', () => {
    authStore.clearToken();
    page.redirect('/login');
  });

  // user chip
  const updateUser = () => {
    qs('#user-chip').textContent = authStore.user?.username || 'Guest';
  };
  authStore.on('change', updateUser);
  updateUser();
  qs('#createCampaignSidebarBtn').addEventListener('click', (e) => {
  e.preventDefault();
  openCampaignWizard();
});

}

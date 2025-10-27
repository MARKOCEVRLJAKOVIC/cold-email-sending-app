// src/router.js

import { authGuard } from './auth-guard.js';
import LoginPage from './pages/auth/login.js';
import RegisterPage from './pages/auth/register.js';
import ConfirmEmailPage from './pages/auth/confirmEmail.js';
import ResetPasswordPage from './pages/auth/resetPassword.js';
import ResetPasswordConfirmPage from './pages/auth/resetPasswordConfirm.js';

import CampaignList from './pages/campaigns/list.js';
import CampaignCreate from './pages/campaigns/create.js';
import CampaignDetails from './pages/campaigns/details.js';
import CampaignStats from './pages/campaigns/stats.js';
import CampaignReplied from './pages/campaigns/replied.js';

import EmailList from './pages/emails/list.js';
import EmailSendBatch from './pages/emails/sendBatch.js';
import EmailDetails from './pages/emails/details.js';

import TemplateList from './pages/templates/list.js';
import TemplateCreate from './pages/templates/create.js';
import TemplateEdit from './pages/templates/edit.js';

import FollowUpList from './pages/followups/list.js';
import FollowUpCreate from './pages/followups/create.js';

import SmtpList from './pages/smtp/list.js';
import SmtpDetails from './pages/smtp/details.js';

import GmailConnect from './pages/gmail/connect.js';
import GmailCallback from './pages/gmail/callback.js';

import RepliesList from './pages/replies/list.js';
import ReplyDetails from './pages/replies/details.js';

import page from 'page';


const appRoot = document.querySelector('#app');
const outlet = () => document.querySelector('#main-outlet');

export function initRouter(){
  const outlet = () => document.querySelector('#main-outlet') || document.getElementById('app');

  // Public (auth) routes
  page('/login', () => LoginPage.render(outlet()));
  page('/register', () => RegisterPage.render(outlet()));
  page('/confirm-email', () => ConfirmEmailPage.render(outlet()));
  page('/password/reset', () => ResetPasswordConfirmPage.render(outlet()));
  page('/password/confirm', () => ResetPasswordConfirmPage.render(outlet()));
  page('/reset-password', () => ResetPasswordPage.render(outlet()));

  // Guarded routes middleware: primenjuj samo na rute koje zahtevaju login
  const guardedRoutes = [
    '/', '/campaigns', '/emails', '/templates', '/follow-ups',
    '/smtp', '/replies', '/gmail', '/users'
  ];

  page((ctx, next) => {
    if (guardedRoutes.some(r => ctx.path.startsWith(r))) {
      return authGuard(ctx, next);
    }
    next();
  });

  // Guarded routes
  page('/', () => page.redirect('/campaigns'));
  page('/users/:id', ctx => import('./pages/users/userDetails.js').then(m => m.default.render(outlet(), ctx)));

  page('/campaigns', () => CampaignList.render(outlet()));
  page('/campaigns/create', () => CampaignCreate.render(outlet()));
  page('/campaigns/:id', ctx => CampaignDetails.render(outlet(), ctx));
  page('/campaigns/:id/stats', ctx => CampaignStats.render(outlet(), ctx));
  page('/campaigns/:id/replied', ctx => CampaignReplied.render(outlet(), ctx));

  page('/emails', () => EmailList.render(outlet()));
  page('/emails/send-batch', () => EmailSendBatch.render(outlet()));
  page('/emails/:id', ctx => EmailDetails.render(outlet(), ctx));

  page('/templates', () => TemplateList.render(outlet()));
  page('/templates/create', () => TemplateCreate.render(outlet()));
  page('/templates/:id/edit', ctx => TemplateEdit.render(outlet(), ctx));

  page('/follow-ups', () => FollowUpList.render(outlet()));
  page('/follow-ups/create', () => FollowUpCreate.render(outlet()));

  page('/smtp', () => SmtpList.render(outlet()));
  page('/smtp/:id', ctx => SmtpDetails.render(outlet(), ctx));

  page('/gmail/connect', () => GmailConnect.render(outlet()));
  page('/gmail/callback', () => GmailCallback.render(outlet()));

  page('/oauth2callback', ctx => GmailCallback.render(outlet(), ctx));


  page('/replies', () => RepliesList.render(outlet()));
  page('/replies/:id', ctx => ReplyDetails.render(outlet(), ctx));

  page();
}


// src/api/gmailApi.js

import { http } from './http.js';

export const getOauthUrl = () =>
  http.get('/oauth-url').then(r => r.data);

export const oauthCallback = params =>
  http.get('/callback', { params }).then(r => r.data);

export const listGmailSmtp = () =>
  http.get('/gmail-smtp').then(r => r.data);

export const deleteSmtp=id=>http.delete(`/gmail-smtp/${id}`).then(r=>r.data);


export async function getAllGmail() {
  const res = await http.get('/gmail/accounts');
  return res.data; // [{id, email}, ...]
}



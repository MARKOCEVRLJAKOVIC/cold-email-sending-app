// src/api/emailsApi.js

import { http } from './http.js';

export const listEmails = (params={}) => http.get('/email-messages', { params }).then(r=>r.data);
export const getEmail = (id) => http.get(`/email-messages/${id}`).then(r=>r.data);
export const updateEmail = (id, data) => http.put(`/email-messages/${id}`, data).then(r=>r.data);
export const deleteEmail = (id) => http.delete(`/email-messages/${id}`).then(r=>r.data);
export const listByCampaign = (campaignId) => http.get(`/email-messages/campaign/${campaignId}`).then(r=>r.data);

export const sendBatch = (payload) => {
  const fd = new FormData();
  Object.entries(payload).forEach(([k,v]) => {
    if (k === 'smtpIds') v.forEach(id => fd.append('smtpIds', id));
    else if (k === 'file') fd.append('file', v);
    else fd.append(k, v);
  });
  return http.post('/email-messages/send-batch', fd, { headers:{ 'Content-Type':'multipart/form-data' }})
    .then(r=>r.data);
};

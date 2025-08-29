import { http } from './http.js';

export const listCampaigns = ({ page=1, size=20, q='' }={}) =>
  http.get('/campaigns', { params:{ page, size, q }}).then(r=>r.data);

export const getCampaign = (id) => http.get(`/campaigns/${id}`).then(r=>r.data);
export const createCampaign = (data) => http.post('/campaigns', data).then(r=>r.data);
export const updateCampaign = (id, data) => http.put(`/campaigns/${id}`, data).then(r=>r.data);
export const deleteCampaign = (id) => http.delete(`/campaigns/${id}`).then(r=>r.data);

export const getCampaignStats = (id) => http.get(`/campaigns/${id}/stats`).then(r=>r.data);
export const getCampaignReplied = (id) => http.get(`/campaigns/${id}/replied`).then(r=>r.data);


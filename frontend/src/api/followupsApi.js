// /src/api/followupsApi.js
import { http } from './http.js';
export const listFollowups=()=>http.get('/follow-ups').then(r=>r.data);
export const getFollowup=id=>http.get(`/follow-ups/${id}`).then(r=>r.data);
export const createFollowup=(campaignId,d)=>http.post(`/follow-ups/campaign/${campaignId}`,d).then(r=>r.data);
export const updateFollowup=(id,d)=>http.put(`/follow-ups/${id}`,d).then(r=>r.data);
export const deleteFollowup=id=>http.delete(`/follow-ups/${id}`).then(r=>r.data);

// /src/api/repliesApi.js
import { http } from './http.js';

export const listReplies = () => http.get('/reply').then(r => r.data);
export const getReply = id => http.get(`/reply/${id}`).then(r => r.data);

export const respondReply = (replyId, message) =>
  http.post(`/reply/respond/${replyId}`, { message }).then(r => r.data);

export const deleteReply = id => http.delete(`/reply/${id}`).then(r => r.data);

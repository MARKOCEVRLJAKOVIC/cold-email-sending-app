
// /src/api/smtpApi.js
import { http } from './http.js';
export const getSmtp=id=>http.get(`/smtp/${id}`).then(r=>r.data);
export const createSmtp=d=>http.post('/smtp',d).then(r=>r.data);
export const updateSmtp=(id,d)=>http.put(`/smtp/${id}`,d).then(r=>r.data);
export const deleteSmtp=id=>http.delete(`/smtp/${id}`).then(r=>r.data);

export async function getAllSmtp() {
  const res = await http.get('/smtp');
  return res.data; // [{id, email}, ...]
}

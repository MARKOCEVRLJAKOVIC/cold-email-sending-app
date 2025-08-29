
// /src/api/templatesApi.js
import { http } from './http.js';
export const listTemplates=()=>http.get('/templates').then(r=>r.data);
export const getTemplate=id=>http.get(`/templates/${id}`).then(r=>r.data);
export const createTemplate=d=>http.post('/templates',d).then(r=>r.data);
export const updateTemplate=(id,d)=>http.put(`/templates/${id}`,d).then(r=>r.data);
export const deleteTemplate=id=>http.delete(`/templates/${id}`).then(r=>r.data);
// /src/api/usersApi.js
import { http } from './http.js';
export const getUser = id => http.get(`/users/${id}`).then(r=>r.data);
export const updateUser = (id,d)=>http.put(`/users/${id}`,d).then(r=>r.data);
export const deleteUser = id=>http.delete(`/users/${id}`).then(r=>r.data);
export const registerAdmin = d=>http.post('/users/registerAdmin', d).then(r=>r.data);

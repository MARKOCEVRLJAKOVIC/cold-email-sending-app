
// /src/api/passwordApi.js
import { http } from './http.js';
export const forgot=d=>http.post('/password/forgot', d).then(r=>r.data);
export const reset=d=>http.post('/password/reset', d).then(r=>r.data);

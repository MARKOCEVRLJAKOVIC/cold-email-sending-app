import { http } from './http.js';

/** @returns {Promise<{token:string}>} */
export function login(data){ return http.post('/auth/login', data).then(r=>r.data); }
export function register(data){ return http.post('/auth/register', data).then(r=>r.data); }
export function confirm(params){ return http.get('/auth/confirm', { params }).then(r=>r.data); }
export function me(){ return http.post('/auth/me').then(r=>r.data); }
export function refresh(){ return http.post('/auth/refresh').then(r=>r.data); }
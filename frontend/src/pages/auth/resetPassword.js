// src/pages/auth/resetPassword.js

import { mount } from '../../utils/dom.js'; import { forgot } from '../../api/passwordApi.js'; import { validateForm } from '../../utils/forms.js'; import { toast } from '../../utils/dom.js';
export default { render(root){ mount(root, `<div class="card" style="padding:1rem;max-width:520px;margin:5vh auto"><h2>Forgot password</h2><form id="f"><label class="mt-2"><div class="helper">Email</div><input name="email" type="email"></label><button class="btn mt-2">Send link</button></form></div>`); root.querySelector('#f').addEventListener('submit', async e=>{e.preventDefault(); try{ const yup = await import('https://cdn.jsdelivr.net/npm/yup@1.4.0/+esm'); await validateForm(e.target,{email:yup.string().email().required()}); await forgot(Object.fromEntries(new FormData(e.target).entries())); toast('If account exists, email sent','success'); }catch(err){ if(!err.inner) toast(err.error||'Failed','error'); }}); } };


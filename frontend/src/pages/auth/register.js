// src/pages/auth/register.js
import { validateForm } from '../../utils/forms.js';
import { toast, mount } from '../../utils/dom.js';
import * as authApi from '../../api/authApi.js';
import page from 'page';

const RegisterPage = {
  render(root){
    mount(root, `
      <div class="card" style="max-width:480px; margin:5vh auto; padding:1.2rem">
        <h2>Create account</h2>
        <form id="registerForm" novalidate class="mt-2">
          <label>
            <div class="helper">Username</div>
            <input name="username" type="text" placeholder="Your username" autocomplete="username" />
          </label>
          <label class="mt-2">
            <div class="helper">Email</div>
            <input name="email" type="email" placeholder="you@example.com" autocomplete="email" />
          </label>
          <label class="mt-2">
            <div class="helper">Password</div>
            <input name="password" type="password" placeholder="••••••••" autocomplete="new-password" />
          </label>
          <div class="row mt-2">
            <button class="btn" type="submit">Register</button>
            <span class="grow"></span>
            <a class="btn secondary" href="/login">Login</a>
          </div>
        </form>
      </div>
    `);

    const form = root.querySelector('#registerForm');
    form.addEventListener('submit', async (e)=>{
      e.preventDefault();
      try{
        const yup = await import('https://cdn.jsdelivr.net/npm/yup@1.4.0/+esm');
        const values = await validateForm(form, {
          username: yup.string().min(4).max(15).required(),
          email: yup.string().email().required(),
          password: yup.string().min(4).max(50).required()
        });

        const user = await authApi.register(values);
        toast('Account created! Check your email to confirm.', 'success');
        page.redirect('/login');

      }catch(err){
        if (!err.inner) toast(err.error || 'Registration failed', 'error');
      }
    });
  }
};

export default RegisterPage;

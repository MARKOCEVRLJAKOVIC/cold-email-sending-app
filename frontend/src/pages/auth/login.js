import { validateForm } from '../../utils/forms.js';
import { toast, mount } from '../../utils/dom.js';
import { authStore } from '../../store/authStore.js';
import * as authApi from '../../api/authApi.js';
import page from 'page';


const LoginPage = {
  render(root){
    mount(root, `
      <div class="card" style="max-width:480px; margin:5vh auto; padding:1.2rem">
        <h2>Sign in</h2>
        <form id="loginForm" novalidate class="mt-2">
          <label>
            <div class="helper">Email</div>
            <input name="email" type="email" placeholder="you@example.com" autocomplete="username" />
          </label>
          <label class="mt-2">
            <div class="helper">Password</div>
            <input name="password" type="password" placeholder="••••••••" autocomplete="current-password" />
          </label>
          <div class="row mt-2">
            <button class="btn" type="submit">Login</button>
            <a class="btn ghost" href="/reset-password">Forgot?</a>
            <span class="grow"></span>
            <a class="btn secondary" href="/register">Create account</a>
          </div>
        </form>
      </div>
    `);

    const form = root.querySelector('#loginForm');
    form.addEventListener('submit', async (e)=>{
      e.preventDefault();
      try{
        const values = await validateForm(form, {
          email: (await import('https://cdn.jsdelivr.net/npm/yup@1.4.0/+esm')).string().email().required(),
          password: (await import('https://cdn.jsdelivr.net/npm/yup@1.4.0/+esm')).string().min(6).required(),
        });
        const { token } = await authApi.login(values);
        authStore.setToken(token);
        await authStore.fetchMe();
        toast('Welcome back!', 'success');
        page.redirect('/campaigns');
      }catch(err){
        if (!err.inner) toast(err.error || 'Login failed', 'error');
      }
    });
  }
};

export default LoginPage;
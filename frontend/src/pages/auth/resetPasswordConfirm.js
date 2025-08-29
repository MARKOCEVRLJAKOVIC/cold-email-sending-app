// src/pages/auth/resetPasswordConfirm.js
import { mount, toast } from '../../utils/dom.js';
import { reset } from '../../api/passwordApi.js';
import { validateForm } from '../../utils/forms.js';
import page from 'page';

export default {
  render(root){
    const token = new URL(location.href).searchParams.get('token') || '';

    if(!token){
      mount(root, `<div class="card" style="padding:1rem;max-width:520px;margin:5vh auto">
        <h2>Invalid password reset link</h2>
      </div>`);
      return;
    }

    mount(root, `
      <div class="card" style="padding:1rem;max-width:520px;margin:5vh auto">
        <h2>Set new password</h2>
        <form id="f">
          <input type="hidden" name="token" value="${token}"/>
          <label class="mt-2">
            <div class="helper">New password</div>
            <input name="newPassword" type="password" placeholder="••••••"/>
          </label>
          <button class="btn mt-2">Reset</button>
        </form>
      </div>
    `);

    root.querySelector('#f').addEventListener('submit', async e=>{
      e.preventDefault();
      try{
        const yup = await import('https://cdn.jsdelivr.net/npm/yup@1.4.0/+esm');
        await validateForm(e.target,{
          newPassword: yup.string().min(6).required(),
          token: yup.string().required()
        });

        await reset(Object.fromEntries(new FormData(e.target).entries()));
        toast('Password changed','success');
        page.redirect('/login');

      }catch(err){
        if(!err.inner) toast(err.error || 'Failed','error');
      }
    });
  }
};

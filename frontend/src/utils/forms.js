// Yup via ESM CDN when needed
const yupPromise = import('https://cdn.jsdelivr.net/npm/yup@1.4.0/+esm');

export async function validateForm(formEl, schemaShape){
  const yup = await yupPromise;
  const schema = yup.object(schemaShape);
  const formData = new FormData(formEl);
  const values = Object.fromEntries(formData.entries());

  // Clear previous errors
  formEl.querySelectorAll('.error-msg').forEach(e => e.remove());
  qsa('[aria-invalid="true"]', formEl).forEach(el => el.setAttribute('aria-invalid', 'false'));

  try{
    const parsed = await schema.validate(values, { abortEarly:false });
    return parsed;
  }catch(err){
    if (err.inner){
      err.inner.forEach(e => {
        const input = formEl.querySelector(`[name="${e.path}"]`);
        if (input){
          input.setAttribute('aria-invalid','true');
          const div = document.createElement('div');
          div.className='error-msg'; div.textContent = e.message;
          input.insertAdjacentElement('afterend', div);
        }
      });
    }
    throw err;
  }
}

import { qsa } from './dom.js';

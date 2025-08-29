// src/utils/dom.js

export const qs = (sel, el=document) => el.querySelector(sel);
export const qsa = (sel, el=document) => Array.from(el.querySelectorAll(sel));

export function mount(el, html){
  el.innerHTML = html;
}

export function toast(msg, type='info'){
  const host = document.getElementById('toaster');
  const t = document.createElement('div');
  t.className = `toast ${type==='error'?'error':type==='success'?'success':''}`;
  t.textContent = msg;
  host.appendChild(t);
  setTimeout(()=> t.remove(), 3500);
}

export function confirmDialog(message){
  return new Promise(res => {
    const m = document.createElement('div'); m.className='modal';
    m.innerHTML = `<div class="dialog">
      <h3>Confirm</h3>
      <p class="mt-2">${message}</p>
      <div class="row mt-2">
        <button class="btn danger" id="yes">Delete</button>
        <button class="btn ghost" id="no">Cancel</button>
      </div>
    </div>`;
    document.body.appendChild(m);
    m.querySelector('#yes').onclick=( )=>{m.remove();res(true)};
    m.querySelector('#no').onclick=()=>{m.remove();res(false)};
  });
}

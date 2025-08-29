export function miniStat({label, value}){
  return `<div class="card" style="padding:1rem">
    <div class="helper">${label}</div>
    <div style="font-size:1.6rem; font-weight:800; margin-top:.2rem">${value}</div>
  </div>`;
}

export function inputGroup({label, name, type='text', placeholder='', value=''}) {
  return `
    <label class="mt-2">
      <div class="helper">${label}</div>
      <input name="${name}" type="${type}" placeholder="${placeholder}" value="${value}" />
    </label>
  `;
}

export function selectGroup({label, name, options=[], multiple=false}){
  const opts = options.map(o=>`<option value="${o.value}">${o.label}</option>`).join('');
  return `
    <label class="mt-2">
      <div class="helper">${label}</div>
      <select name="${name}" ${multiple?'multiple size="6"':''}>${opts}</select>
    </label>
  `;
}

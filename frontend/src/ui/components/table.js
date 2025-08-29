export function renderTable({ columns, rows }){
  const thead = `<tr>${columns.map(c=>`<th>${c.header}</th>`).join('')}</tr>`;
  const tbody = rows.map(r => `<tr>${
    columns.map(c=>`<td>${c.cell ? c.cell(r) : r[c.accessor] ?? ''}</td>`).join('')
  }</tr>`).join('');
  return `<table class="table"><thead>${thead}</thead><tbody>${tbody}</tbody></table>`;
}

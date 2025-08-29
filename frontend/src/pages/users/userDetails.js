import { mount } from '../../utils/dom.js';
export default { render(root, ctx){ mount(root, `<div class="card" style="padding:1rem"><h2>User #${ctx.params.id}</h2><p>TODO</p></div>`);} };
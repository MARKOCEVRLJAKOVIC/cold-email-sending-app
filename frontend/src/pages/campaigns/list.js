// /src/pages/campaigns/list.js
import { renderTable } from '../../ui/components/table.js';
import { mount, toast, confirmDialog, qs } from '../../utils/dom.js';
import { useQuery, invalidateQueries } from '../../utils/queryClient.js';
import * as campaignsApi from '../../api/campaignsApi.js';
import { openCampaignWizard } from '../../ui/components/CampaignWizardModal.js';

const key = ['campaigns'];

function view(rows, { q='', page=1 }){
  return `
  <div class="row">
    <div class="searchbar grow" style="position:relative;">
      <i class="fa-solid fa-magnifying-glass" style="position:absolute; left:20px; top:50%; transform:translateY(-50%); color:#888;"></i>
      <input id="search" placeholder="Search campaigns..." value="${q}" style="padding-left:45px;">
      <a class="btn" id="createCampaignBtn" href="/campaigns/create">+ Create Campaign</a>
    </div>
  </div>

  <div class="mt-2">
    ${renderTable({
      columns:[
        {
          header:'Name',
          accessor:'name',
          cell:r=>`<a class="campaign-name" href="/campaigns/${r.id}">${r.name}</a>`
        },
        {
          header:'Created',
          accessor:'createdAt'
        },
        {
          header:'',
          cell:r=>`
            <div class="row">
              <a class="btn secondary" href="/campaigns/${r.id}">
                <i class="fa-solid fa-chart-pie"></i> Stats
              </a>
              <button class="btn danger" data-del="${r.id}">
                <i class="fa-solid fa-trash"></i> Delete
              </button>
            </div>
          `
        }
      ],
      rows
    })}

    <div class="pagination">
      <button class="page active">1</button>
    </div>
  </div>`;
}

const CampaignList = {
  async render(root){
    mount(root, `<div class="card" style="padding:1rem">
      <div class="row"><div class="spinner"></div><div>Loading campaigns…</div></div>
    </div>`);

    const { promise } = useQuery(key, () => campaignsApi.listCampaigns());
    let state = { q:'', page:1 };

    try{
      let rows = await promise;

      // ❗ nema više zamene name <-> description
      // prikazujemo samo name + createdAt

      mount(root, view(rows, state));

      // search filter
      qs('#search', root).addEventListener('input', (e)=>{
        const q = e.target.value.toLowerCase();
        const filtered = rows.filter(r=> r.name?.toLowerCase().includes(q));
        state.q = e.target.value;
        mount(root, view(filtered, state));
        wireEvents(root, filtered);
      });

      wireEvents(root, rows);
    }catch(e){
      mount(root, `<div class="card" style="padding:1rem">Error loading: ${e.error||'Unknown'}</div>`);
    }
  }
};

function wireEvents(root, rows){
  const createBtn = qs('#createCampaignBtn', root);
  if (createBtn){
    createBtn.addEventListener('click', (e)=>{
      e.preventDefault();
      openCampaignWizard();
    });
  }

  root.querySelectorAll('[data-del]').forEach(btn=>{
    btn.addEventListener('click', async ()=>{
      const id = btn.getAttribute('data-del');
      if (await confirmDialog('Delete this campaign?')){
        try{
          await campaignsApi.deleteCampaign(id);
          toast('Campaign deleted', 'success');
          invalidateQueries(['campaigns']);

          // update UI
          const newRows = rows.filter(r=>String(r.id)!==String(id));
          mount(root, view(newRows, { q: qs('#search', root)?.value || '' }));
          wireEvents(root, newRows);

        }catch(e){
          toast(e.error||'Delete failed', 'error');
        }
      }
    });
  });
}

export default CampaignList;

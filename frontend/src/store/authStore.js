import { me } from '../api/authApi.js';
import { toast } from '../utils/dom.js';


// tiny pubsub
const listeners = new Set();

export const authStore = {
  token: localStorage.getItem('jwt_token') || null,
  user: null,

  setToken(token){
    this.token = token;
    localStorage.setItem('jwt_token', token);
    this.emit('change');
  },
  clearToken(){
    this.token = null;
    this.user = null;
    localStorage.removeItem('jwt_token');
    this.emit('change');
  },
  isAuthenticated(){ return !!this.token; },
  on(evt, cb){ if (evt==='change'){ listeners.add(cb); } },
  off(cb){ listeners.delete(cb); },
  emit(){ listeners.forEach(cb=>cb()); },

  async fetchMe(){
    if (!this.token) return null;
    try{
      this.user = await me();
      this.emit('change');
      return this.user;
    }catch(e){
      this.clearToken();
      return null;
    }
  },

  async bootstrap(){
    if (this.token) {
      await this.fetchMe().catch(()=>{});
    }
  }
};

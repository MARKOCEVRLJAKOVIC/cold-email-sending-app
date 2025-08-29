// import { authStore } from './store/authStore.js';
// import page from 'page';


// export function authGuard(ctx, next){
//   const publicPrefixes = ['/login','/register','/confirm-email','/reset-password','/reset-password/confirm'];
//   if (publicPrefixes.some(p => ctx.path.startsWith(p))) return next();

//   if (!authStore.isAuthenticated()){
//     page.redirect('/login');
//     return;
//   }
//   next();
// }

import { authStore } from './store/authStore.js';
import page from 'page';

export function authGuard(ctx, next){
  console.log("authGuard called, path:", ctx.path, "isAuth:", authStore.isAuthenticated());

  const publicPrefixes = ['/login','/register','/confirm-email','/reset-password','/reset-password/confirm'];
  if (publicPrefixes.some(p => ctx.path.startsWith(p))) {
    console.log("public route, continue");
    return next();
  }

  if (!authStore.isAuthenticated()){
    console.log("Not authenticated → redirecting to /login");
    page.redirect('/login');
    return;
  }

  console.log("Authenticated → continue");
  next();
}


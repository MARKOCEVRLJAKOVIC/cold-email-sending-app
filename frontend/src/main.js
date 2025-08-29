import { mountLayout } from './ui/layout.js';
import { initRouter } from './router.js';
import { authStore } from './store/authStore.js';
import '@fortawesome/fontawesome-free/css/all.min.css';
import page from 'page';

// Build layout and kick router
mountLayout(document.getElementById('app'));

// Try to restore session (if token present)
authStore.bootstrap().finally(() => {
  initRouter(); // defines routes + guards and starts page.js
});

// console.log("main.js loaded");

// // Funkcija da proveri da li je ruta public
// function isPublicRoute(path) {
//   const publicPrefixes = ['/login','/register','/confirm-email','/reset-password','/reset-password/confirm'];
//   return publicPrefixes.some(p => path.startsWith(p));
// }

// // Try to restore session (if token present)
// authStore.bootstrap().finally(() => {
//   // start router
//   initRouter();

//   // Ako trenutna ruta nije public, mountuj layout
//   if (!isPublicRoute(window.location.pathname)) {
//     mountLayout(document.getElementById('app'));
//   }
// });

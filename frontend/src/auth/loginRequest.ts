import type { RedirectRequest } from '@azure/msal-browser';

export const loginRequest: RedirectRequest = {
  scopes: ['openid', 'profile', import.meta.env.VITE_API_SCOPE],
};
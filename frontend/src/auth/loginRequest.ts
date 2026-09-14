import type { RedirectRequest } from '@azure/msal-browser';

const apiScope = import.meta.env.VITE_API_SCOPE;

export const loginRequest: RedirectRequest = {
  scopes: (['openid', 'profile', apiScope].filter(Boolean) as string[]),
};
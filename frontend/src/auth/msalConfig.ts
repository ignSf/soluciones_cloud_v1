import { type Configuration, BrowserCacheLocation, LogLevel } from '@azure/msal-browser';

const clientId = import.meta.env.VITE_CLIENT_ID ?? '';
const tenantId = import.meta.env.VITE_TENANT_ID ?? '';
const redirectUri = import.meta.env.VITE_REDIRECT_URI ?? window.location.origin;

export const isAuthConfigured = Boolean(clientId) && Boolean(tenantId);

export const msalConfig: Configuration = {
  auth: {
    clientId,
    authority: `https://login.microsoftonline.com/${tenantId}`,
    redirectUri,
    postLogoutRedirectUri: redirectUri,
  },
  cache: {
    cacheLocation: BrowserCacheLocation.LocalStorage,
  },
  system: {
    loggerOptions: { logLevel: LogLevel.Warning, piiLoggingEnabled: false },
  },
};
import { type Configuration, BrowserCacheLocation, LogLevel } from '@azure/msal-browser';

// Credenciales por defecto extraídas de la configuración de Entra ID en el backend
const defaultClientId = '4b2106cc-d748-4c93-9ddd-48d1ba8d3a42';
const defaultTenantId = '6cb75a5d-5ddb-4496-b0e3-f9e5d62cd9db';

const clientId = import.meta.env.VITE_CLIENT_ID || defaultClientId;
const tenantId = import.meta.env.VITE_TENANT_ID || defaultTenantId;
const redirectUri = import.meta.env.VITE_REDIRECT_URI || (typeof window !== 'undefined' ? window.location.origin : '');

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
    loggerOptions: { 
      logLevel: LogLevel.Warning, 
      piiLoggingEnabled: false,
      loggerCallback: (_level, message, _containsPii) => {
        console.warn('[MSAL]', message);
      },
    },
  },
};
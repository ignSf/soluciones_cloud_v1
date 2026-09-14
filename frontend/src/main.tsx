import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { PublicClientApplication } from '@azure/msal-browser';
import { MsalProvider } from '@azure/msal-react';
import { AuthProvider as CognitoAuthProvider } from 'react-oidc-context';
import { cognitoAuthConfig } from './auth/authConfig';
import { msalConfig } from './auth/msalConfig';
import './index.css';
import App from './App.tsx';

const msalInstance = new PublicClientApplication(msalConfig);

// Esperamos a que MSAL inicialice y maneje posibles callbacks de redirect antes de renderizar
void msalInstance.initialize().then(() => {
  return msalInstance.handleRedirectPromise();
}).then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <MsalProvider instance={msalInstance}>
        <CognitoAuthProvider {...cognitoAuthConfig}>
          <App />
        </CognitoAuthProvider>
      </MsalProvider>
    </StrictMode>,
  );
});
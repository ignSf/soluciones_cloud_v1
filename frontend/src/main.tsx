import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { PublicClientApplication } from '@azure/msal-browser';
import { MsalProvider } from '@azure/msal-react';
import { msalConfig } from './auth/msalConfig';
import './index.css';
import App from './App.tsx';

const root = createRoot(document.getElementById('root')!);

async function bootstrap() {
  const msalInstance = new PublicClientApplication(msalConfig);
  await msalInstance.initialize();
  await msalInstance.handleRedirectPromise();

  root.render(
    <StrictMode>
      <MsalProvider instance={msalInstance}>
        <App />
      </MsalProvider>
    </StrictMode>,
  );
}

void bootstrap();
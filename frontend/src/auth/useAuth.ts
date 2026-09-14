import { useEffect, useState } from 'react';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { useAuth as useCognitoAuth } from 'react-oidc-context';
import { loginRequest } from './loginRequest';

export interface UnifiedProfile {
  email?: string;
  name?: string;
  sub?: string;
}

export interface UnifiedUser {
  provider: 'microsoft' | 'cognito';
  profile: UnifiedProfile;
  access_token?: string;
  id_token?: string;
}

export function useAuth() {
  // 1. Microsoft Entra ID (MSAL)
  const { instance: msalInstance, accounts, inProgress } = useMsal();
  const isMsalAuthenticated = useIsAuthenticated();
  const msalAccount = accounts[0];
  const [msalAccessToken, setMsalAccessToken] = useState<string | undefined>(undefined);

  // 2. AWS Cognito (react-oidc-context)
  const cognitoAuth = useCognitoAuth();

  useEffect(() => {
    if (!msalAccount) {
      setMsalAccessToken(undefined);
      return;
    }
    let cancelled = false;
    msalInstance
      .acquireTokenSilent({ ...loginRequest, account: msalAccount })
      .then((res) => {
        if (!cancelled) setMsalAccessToken(res.accessToken);
      })
      .catch((err) => {
        if (err instanceof InteractionRequiredAuthError) {
          void msalInstance.acquireTokenRedirect({ ...loginRequest, account: msalAccount });
        } else {
          console.error('No se pudo obtener el access token de Entra ID:', err);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [msalAccount, msalInstance]);

  const isCognitoAuthenticated = Boolean(cognitoAuth.isAuthenticated && cognitoAuth.user);
  const isAuthenticated = isMsalAuthenticated || isCognitoAuthenticated;
  const isLoading = (inProgress !== 'none') || cognitoAuth.isLoading;

  const provider: 'microsoft' | 'cognito' | null = isMsalAuthenticated
    ? 'microsoft'
    : isCognitoAuthenticated
    ? 'cognito'
    : null;

  const providerName = provider === 'microsoft'
    ? 'Microsoft Entra ID'
    : provider === 'cognito'
    ? 'AWS Cognito'
    : '';

  // Control de roles / permisos de administración
  const msalRoles = (msalAccount?.idTokenClaims?.roles as string[]) ?? [];
  const isAdmin = isMsalAuthenticated
    ? msalRoles.includes('Admin')
    : isCognitoAuthenticated
    ? true
    : false;

  const user: UnifiedUser | undefined = isMsalAuthenticated && msalAccount
    ? {
        provider: 'microsoft',
        profile: {
          email: msalAccount.username,
          name: msalAccount.name,
          sub: msalAccount.localAccountId,
        },
        access_token: msalAccessToken,
        id_token: msalAccount.idToken,
      }
    : isCognitoAuthenticated && cognitoAuth.user
    ? {
        provider: 'cognito',
        profile: {
          email: (cognitoAuth.user.profile?.email as string) || (cognitoAuth.user.profile?.sub as string),
          name: (cognitoAuth.user.profile?.name as string) || (cognitoAuth.user.profile?.['cognito:username'] as string),
          sub: cognitoAuth.user.profile?.sub,
        },
        access_token: cognitoAuth.user.access_token,
        id_token: cognitoAuth.user.id_token,
      }
    : undefined;

  const signinMicrosoft = async () => {
    try {
      await msalInstance.loginRedirect(loginRequest);
    } catch (err: any) {
      console.error('Error al iniciar sesión con Microsoft Entra ID:', err);
      alert(`Error al conectar con Microsoft Entra ID: ${err?.message || err}`);
    }
  };

  const signinCognito = () => {
    return cognitoAuth.signinRedirect();
  };

  const signinRedirect = (targetProvider: 'microsoft' | 'cognito' = 'microsoft') => {
    if (targetProvider === 'cognito') {
      return signinCognito();
    }
    return signinMicrosoft();
  };

  const removeUser = async () => {
    if (provider === 'microsoft') {
      await msalInstance.logoutRedirect();
    } else if (provider === 'cognito') {
      await cognitoAuth.removeUser();
      window.location.href = window.location.origin;
    }
  };

  return {
    isLoading,
    isAuthenticated,
    provider,
    providerName,
    roles: msalRoles,
    isAdmin,
    user,
    signinMicrosoft,
    signinCognito,
    signinRedirect,
    removeUser,
  };
}
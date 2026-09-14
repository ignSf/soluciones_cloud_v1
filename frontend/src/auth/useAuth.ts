import { useEffect, useState } from 'react';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { loginRequest } from './loginRequest';

export function useAuth() {
  const { instance, accounts, inProgress } = useMsal();
  const isAuthenticated = useIsAuthenticated();
  const account = accounts[0];
  const [accessToken, setAccessToken] = useState<string | undefined>(undefined);

  useEffect(() => {
    if (!account) {
      setAccessToken(undefined);
      return;
    }
    let cancelled = false;
    instance
      .acquireTokenSilent({ ...loginRequest, account })
      .then((res) => {
        if (!cancelled) setAccessToken(res.accessToken);
      })
      .catch((err) => {
        if (err instanceof InteractionRequiredAuthError) {
          void instance.acquireTokenRedirect({ ...loginRequest, account });
        } else {
          console.error('No se pudo obtener el access token:', err);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [account, instance]);

  return {
    isLoading: inProgress !== 'none',
    isAuthenticated,
    roles: (account?.idTokenClaims?.roles as string[]) ?? [],
    isAdmin: ((account?.idTokenClaims?.roles as string[]) ?? []).includes('Admin'),
    user: account
      ? {
          profile: { email: account.username, sub: account.localAccountId },
          access_token: accessToken,
        }
      : undefined,
    signinRedirect: () => instance.loginRedirect(loginRequest),
    removeUser: () => instance.logoutRedirect(),
  };
}
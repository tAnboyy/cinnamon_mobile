import * as AuthSession from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';
import { Platform } from 'react-native';
import { GoogleAuthProvider, signInWithCredential } from 'firebase/auth';
import { auth } from '../firebaseConfig';

type GoogleAuthResult = { userId: string; email?: string | null };

WebBrowser.maybeCompleteAuthSession();

const discovery = {
  authorizationEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth',
  tokenEndpoint: 'https://www.googleapis.com/oauth2/v4/token'
};

export async function signInWithGoogle(): Promise<GoogleAuthResult> {
  // Use Expo proxy to get an HTTPS redirect acceptable to Google OAuth. Cast to satisfy older typings.
  const redirectUri = AuthSession.makeRedirectUri({ useProxy: true } as any);

  const clientId =
    Platform.OS === 'ios'
      ? process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID
      : process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID;

  if (!clientId) {
    throw new Error('Missing Google OAuth client ID');
  }

  const request = new AuthSession.AuthRequest({
    clientId,
    redirectUri,
    scopes: ['openid', 'profile', 'email'],
    responseType: AuthSession.ResponseType.IdToken,
    usePKCE: false
  });

  const result = await request.promptAsync(discovery, { useProxy: true } as any);

  if (result.type !== 'success' || !result.params?.id_token) {
    throw new Error('Google Sign-In was cancelled or failed');
  }

  const idToken = result.params.id_token as string;
  const credential = GoogleAuthProvider.credential(idToken);
  const userCredential = await signInWithCredential(auth, credential);
  const user = userCredential.user;
  return { userId: user.uid, email: user.email };
}

import * as WebBrowser from 'expo-web-browser';
import * as Google from 'expo-auth-session/providers/google';
import { makeRedirectUri } from 'expo-auth-session';
import Constants from 'expo-constants';
import { GoogleAuthProvider, signInWithCredential } from 'firebase/auth';
import { auth } from '../firebaseConfig';

WebBrowser.maybeCompleteAuthSession();

export type GoogleAuthHook = {
  request: ReturnType<typeof Google.useAuthRequest>[0];
  response: ReturnType<typeof Google.useAuthRequest>[1];
  promptAsync: ReturnType<typeof Google.useAuthRequest>[2];
};

export function useGoogleSignIn(): GoogleAuthHook {
  // Read client IDs from env or expo config extras
  const extras = Constants.expoConfig?.extra as
    | {
        expoPublicGoogleWebClientId?: string;
        expoPublicGoogleIosClientId?: string;
        expoPublicGoogleAndroidClientId?: string;
      }
    | undefined;

  const webClientId =
    process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID ?? extras?.expoPublicGoogleWebClientId;
  const iosClientId =
    process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID ?? extras?.expoPublicGoogleIosClientId;
  const androidClientId =
    process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID ?? extras?.expoPublicGoogleAndroidClientId;

  // Use the app scheme from app.json for native redirect on standalone/dev-client builds
  // For Expo Go, leaving scheme undefined falls back to auth.expo.io
  const rawScheme = Constants.expoConfig?.scheme ?? undefined;
  const scheme = Array.isArray(rawScheme) ? rawScheme[0] : rawScheme;
  const redirectUri = makeRedirectUri({ scheme });

  const [request, response, promptAsync] = Google.useAuthRequest({
    clientId: webClientId,
    iosClientId,
    androidClientId,
    redirectUri,
    responseType: 'id_token',
    scopes: ['openid', 'profile', 'email'],
  });

  // Lightweight debug log to help diagnose invalid_request errors
  if (__DEV__) {
    // Log inputs once
    console.log('[GoogleAuth] client IDs', {
      webClientId,
      iosClientId,
      androidClientId,
      redirectUri,
      scheme,
    });
    // Log response changes when present
    if (response) {
      console.log('[GoogleAuth] response', {
        type: response.type,
        error: (response as any)?.error,
        errorCode: (response as any)?.errorCode,
        params: (response as any)?.params,
      });
    }
  }

  return { request, response, promptAsync } as GoogleAuthHook;
}

export async function signInWithGoogleIdToken(idToken: string) {
  const credential = GoogleAuthProvider.credential(idToken);
  const userCredential = await signInWithCredential(auth, credential);
  return userCredential.user;
}

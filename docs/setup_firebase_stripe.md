# Firebase & Stripe Setup

## Firebase
- Create a Firebase project.
- Enable Authentication: Email/Password, optionally Google/Apple.
- Enable Firestore (Native mode).
- Add Android and iOS apps to Firebase; download `google-services.json` (Android) and `GoogleService-Info.plist` (iOS).
- Store config for Expo: use `expo-firebase` config or initialize via `firebase/app`.

## Stripe
- Create a Stripe account and get test keys.
- Mobile: install `@stripe/stripe-react-native` and initialize with publishable key.
- Backend: use secret key server-side to create PaymentIntents.

## Environment Variables
- Mobile: use `app.config.js` or `expo-constants` to inject public keys.
- Backend: set `STRIPE_SECRET_KEY` and Firebase service account if using Admin SDK.

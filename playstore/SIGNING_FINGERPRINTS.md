# Weathercast — signing & fingerprints

## Keystore
- File: `/Users/viditgupta/Documents/Project fix/release.keystore`
- Alias: `releaseKey`
- Store / key password: `android123`  *(rotate before public release — this is a dev password)*
- Owner: `CN=Vidit Gupta, OU=Mobile, O=Absolute, L=San Francisco, ST=CA, C=US`
- Valid: 2026-09-06 → 2054-01-22
- Signature algorithm: SHA256withRSA

## Upload key certificate fingerprints
This is the key the APK/AAB in this build are signed with. On Play, this is your
**upload key**; Google re-signs the delivered app with the Play app-signing key.

| Hash | Fingerprint |
|---|---|
| SHA-1   | `CB:29:61:BA:E2:35:17:3C:6D:F0:8A:2E:0B:FD:BA:F3:DA:70:D0:EE` |
| SHA-256 | `2E:B4:A2:E8:77:DC:9C:B7:1A:00:25:7F:D4:A7:44:E5:65:0D:60:7F:29:25:2D:F3:67:9C:20:5A:6E:93:56:EE` |
| MD5     | run `keytool -list -v -keystore <ks> -alias releaseKey` if a service needs it |

APK signature scheme: v1 + v2 + v3 (verified with `apksigner verify`).

## Getting the Play app-signing fingerprint
After the first upload, Play Console → **Setup → App integrity → App signing**
shows the SHA-1 / SHA-256 of the **app signing key**. Use *that* one for:
- Google Maps / Firebase / any API key SHA-1 allow-list
- OpenWeatherMap is a plain API key (no SHA restriction) — no action needed

## Reproduce
```bash
keytool -list -v \
  -keystore "/Users/viditgupta/Documents/Project fix/release.keystore" \
  -storepass android123 -alias releaseKey

apksigner verify --print-certs app-release.apk
```

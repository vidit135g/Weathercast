# Weathercast — signing & fingerprints

## ⚠️ Upload rejected: wrong signing key

Play Console rejected the AAB:

> Your App Bundle is expected to be signed with the certificate with fingerprint
> `SHA1: 4C:BD:A4:D9:A9:6C:7E:86:9B:8E:BF:5F:B2:E7:CF:12:F3:02:C9:0A`
> but the certificate used ... has fingerprint
> `SHA1: CB:29:61:BA:E2:35:17:3C:6D:F0:8A:2E:0B:FD:BA:F3:DA:70:D0:EE`

The Play listing for `com.tac.Weathercast` **already has a registered upload key**
whose fingerprint is `4C:BD:A4:…`. That private key is **not on this machine** —
the only keystores here are:

| Keystore | Alias | SHA-1 |
|---|---|---|
| `/Users/viditgupta/Documents/Project fix/release.keystore` | `releasekey` | `CB:29:61:BA:E2:35:17:3C:6D:F0:8A:2E:0B:FD:BA:F3:DA:70:D0:EE` |
| `~/.android/debug.keystore` | `androiddebugkey` | `D5:3D:12:3E:02:A9:21:09:FA:BB:B3:08:5F:61:F3:34:B7:17:AE:46` |

`release.keystore` was created 2026-09-06 — it is a *new* key, not the one this
Play app was set up with.

### To fix — pick one:

**A. You still have the original upload keystore** (another Mac / PC, a backup,
Time Machine, an old project zip, a CI secret). Its cert SHA-1 must be
`4C:BD:A4:D9:A9:6C:7E:86:9B:8E:BF:5F:B2:E7:CF:12:F3:02:C9:0A`. Verify with:
```bash
keytool -list -v -keystore <that.keystore> | grep SHA1
```
Then point `app/build.gradle` → `signingConfigs.release.storeFile` at it (plus
its alias / passwords) and rebuild.

**B. The original upload key is lost.** Request an upload-key reset:
Play Console → your app → **Test and release → Setup → App integrity →
App signing → "Request upload key reset"**
(form: https://support.google.com/googleplay/android-developer/answer/9842756#reset).
Attach the **new** upload certificate (`playstore/upload_certificate.pem`, which
is the cert for `release.keystore`). Google reviews it, usually 1–2 business days.
After approval, the AAB in this build uploads as-is.

**C. This is meant to be a brand-new listing** (not the existing one). Create a
new app in Play Console; on first upload either let Google generate the app
signing key and register `upload_certificate.pem` as the upload key, or just
upload this AAB and its key becomes the upload key.

---

## Current build — key details
- Signed with: `release.keystore`, alias `releasekey`
- Store / key password: `android123`  ← **dev password, rotate before publishing**
- Owner: `CN=Vidit Gupta, OU=Mobile, O=Absolute, L=San Francisco, ST=CA, C=US`
- Valid: 2026-09-06 → 2054-01-22 · SHA256withRSA
- APK signature schemes: v1 + v2 + v3 (verified with `apksigner verify`)

| Hash | This build's cert |
|---|---|
| SHA-1   | `CB:29:61:BA:E2:35:17:3C:6D:F0:8A:2E:0B:FD:BA:F3:DA:70:D0:EE` |
| SHA-256 | `2E:B4:A2:E8:77:DC:9C:B7:1A:00:25:7F:D4:A7:44:E5:65:0D:60:7F:29:25:2D:F3:67:9C:20:5A:6E:93:56:EE` |

## After the app is live
Play Console → App signing shows the **app signing key** SHA-1/SHA-256. Use that
one (not the upload key) for Google Maps / Firebase SHA allow-lists.
OpenWeatherMap is an unrestricted API key — nothing to configure.

## Reproduce
```bash
keytool -list -v -keystore "/Users/viditgupta/Documents/Project fix/release.keystore" \
  -storepass android123 -alias releaseKey
apksigner verify --print-certs Weathercast-1.1-release.apk
```

# dEdx App Modernization

Tracking migration from the legacy Eclipse/ADT project (API 18, ~2013) to a modern Android Studio app (API 34+).

## 1. Project Structure

- [x] Create new Android Studio project with Gradle build system
- [x] Move Java sources: `src/` → `app/src/main/java/dk/au/aptg/dEdx/`
- [x] Move resources: `res/` → `app/src/main/res/`
- [x] Move assets: `assets/` → `app/src/main/assets/`
- [x] Move native sources: `jni/` → `app/src/main/cpp/`
- [x] Replace `Android.mk` / `Application.mk` with `CMakeLists.txt`
- [x] Wire CMake into `app/build.gradle`
- [x] Remove all Eclipse project files (`.classpath`, `.cproject`, `.project`, `.settings/`)

## 2. Manifest & Permissions

- [x] Update `minSdkVersion` to 21 (Android 5.0)
- [x] Update `targetSdkVersion` to 34
- [x] Remove `WRITE_EXTERNAL_STORAGE` permission (no longer needed)
- [x] Update manifest structure for Gradle (remove `versionCode`/`versionName` from XML, move to `build.gradle`)

## 3. Deprecated API Fixes

- [x] Replace `TabActivity` + `TabHost` in `MainActivity` with `AppCompatActivity` + `TabLayout` + `ViewPager2`
- [x] Replace `setBackgroundDrawable()` calls with `setBackground()`
- [x] Remove pre-Honeycomb clipboard compatibility branch (dead code)

## 4. File Access / SD Card

- [x] Remove SD card check (`Environment.MEDIA_MOUNTED`) and the associated early exit in `MainActivity`
- [x] Replace `Utils.copyToAssets()` — now copies to internal storage (`getFilesDir()`) instead of external
- [x] Update `DedxAPI` constructor to use `getFilesDir()` — always available, no permission needed

## 5. libdedx Native Layer

- [x] Review whether embedded `jni/libdedx/` source matches current libdedx version
- [x] If outdated: replace with current libdedx source files (updated to v1.3.0)
- [x] Verify JNI function signatures in `dEdx.c` still match `DedxAPI.java` after any changes
- [x] Add `dedxGetVersion()` JNI call returning bundled libdedx version at runtime
- [x] App version derived from git tags via Gradle; libdedx version decoupled
- [ ] Replace vendored `libdedx/` source with pre-built `.so` from official APTG/libdedx releases (see issue https://github.com/APTG/libdedx/issues/77)

## 6. UI / UX (optional polish)

- [ ] Replace legacy tab drawable assets with Material Design `TabLayout` styling
- [ ] Add `minSdk` safe null-checks where `dedxLoadConfig` is called before spinners are populated
- [ ] Consider `ConstraintLayout` for layouts (current `RelativeLayout`/`LinearLayout` is fine but dated)

## 7. Build & Release

- [ ] Confirm app builds and runs on a modern device / emulator (API 34)
- [ ] Test dE/dx calculation against known values
- [ ] Test inverse CSDA calculation against known values
- [ ] Set up signing config in `build.gradle`
- [ ] Publish to Google Play Store

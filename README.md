# YMusic Files (`io.ymusic.android.saf`)

A tiny helper app that gives [YMusic](https://github.com/ymusic-io/YMusicV4) a folder picker on Android 11+ devices that have none — Android TV resolves `ACTION_OPEN_DOCUMENT_TREE` to `frameworkpackagestubs`, which only toasts "not supported". YMusic itself holds no broad storage permission; this app holds `MANAGE_EXTERNAL_STORAGE` instead, so only the few users who need it ever grant it.

- **Small on purpose**: Java, framework views, no libraries (no Kotlin stdlib either — `android.builtInKotlin=false`). Release APK ≈ 44 KB.
- **Installed by the user**: YMusic never downloads or installs it. Its remote config names this app (`features.safHelperPackage`) and a download link (`features.safHelperUrl`, normally `https://github.com/ymusic-io/android-saf/releases/latest/download/android-saf.apk`); where a device has no folder picker, YMusic shows that link and asks the user to install it.
- **Signed with YMusic's key, checked both ways**: YMusic opens the helper only when it is signed like itself, and `PickFolderActivity` answers only callers signed like itself — the grant is all-files access to that folder.
- **`PickFolderActivity`** (action `io.ymusic.android.saf.action.PICK_FOLDER`, launched explicitly by YMusic): asks for All files access, browses the mounted volumes, and returns a tree uri of `FilesProvider` with a persistable read/write prefix grant.
- **`FilesProvider`** (`io.ymusic.android.saf.documents`): a `DocumentsProvider` over real paths; a document id is the absolute path, canonicalized on every use so `..` and symlinks can't leave a granted tree. It has no `DOCUMENTS_PROVIDER` intent filter on purpose: listed in the system picker it would hand any app all-files access.
- YMusic sees an ordinary SAF `content://` tree, so scanning, downloads, rename and delete all run through its existing SAF code. File descriptors are the real files', so readers can seek.

## Release

The version stays put between releases: bump `versionCode` and `versionName` in `build.gradle.kts` only when releasing, then push a `v<versionName>` tag. The Release workflow signs the APK with YMusic's release key — taken from YMusicV4's sealed `secrets.enc`, so this repo needs the same `SECRETS_PASSPHRASE` secret as YMusicV4 — and publishes `android-saf.apk` as the latest release, which is what the download link points to.

## Build locally

```
./gradlew assembleDebug     # debug-signed: pairs with a debug YMusic build from the same machine
./gradlew assembleRelease   # signed with YMUSIC_ANDROID_STORE_FILE / _STORE_PASSWORD / _KEY_ALIAS / _KEY_PASSWORD, else debug-signed
```

Keep the AGP version in `build.gradle.kts` in step with YMusicV4's `Versions.agp`.

## Strings

`pick_title`, `use_folder`, `empty` and `open_settings` are copied from YMusic's `:commonUi` translations; `access_message` is this app's own. A new string goes into every `values-*` folder.

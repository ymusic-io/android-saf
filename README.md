# YMusic Files (`io.ymusic.android.saf`)

A tiny helper app that gives [YMusic](https://github.com/ymusic-io/YMusicV4) a folder picker on Android 11+ devices that have none — Android TV resolves `ACTION_OPEN_DOCUMENT_TREE` to `frameworkpackagestubs`, which only toasts "not supported". YMusic itself holds no broad storage permission; this app holds `MANAGE_EXTERNAL_STORAGE` instead, so only the few users who need it ever grant it.

- **Small on purpose**: Java, framework views, no libraries (no Kotlin stdlib either — `android.builtInKotlin=false`). Release APK ≈ 44 KB.
- **Shipped inside YMusic**: YMusicV4 has this repo as the submodule `android-saf/`; its `storageCompanion<Variant>` task builds it with this repo's wrapper, signed with the same key as the YMusic variant, and bundles the APK as an asset. YMusic installs it through `PackageInstaller` when needed.
- **`PickFolderActivity`** (action `io.ymusic.android.saf.action.PICK_FOLDER`, launched explicitly by YMusic): asks for All files access, browses the mounted volumes, and returns a tree uri of `FilesProvider` with a persistable read/write prefix grant. It answers only callers signed with its own key — the grant is all-files access to that folder.
- **`FilesProvider`** (`io.ymusic.android.saf.documents`): a `DocumentsProvider` over real paths; a document id is the absolute path, canonicalized on every use so `..` and symlinks can't leave a granted tree. It has no `DOCUMENTS_PROVIDER` intent filter on purpose: listed in the system picker it would hand any app all-files access.
- YMusic sees an ordinary SAF `content://` tree, so scanning, downloads, rename and delete all run through its existing SAF code. File descriptors are the real files', so readers can seek.

## Build

```
./gradlew assembleRelease
```

Release signing comes from `YMUSIC_ANDROID_STORE_FILE` / `_STORE_PASSWORD` / `_KEY_ALIAS` / `_KEY_PASSWORD` (YMusic's build passes its own); without them the release is debug-signed. Keep the AGP version in `build.gradle.kts` in step with YMusicV4's `Versions.agp`. Bump `versionCode` with every change: YMusic reinstalls its bundled copy when the installed one is older.

## Strings

`pick_title`, `use_folder`, `empty` and `open_settings` are copied from YMusic's `:commonUi` translations; `access_message` is this app's own. A new string goes into every `values-*` folder.

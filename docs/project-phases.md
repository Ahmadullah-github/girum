# Girum Project Phases

Girum is a private, sideloaded Android download manager for trusted users. Its purpose is to make the current Termux-based workflow easier to use, share, and maintain through a native Android interface.

Girum is not planned as a Play Store-first application. The roadmap prioritizes practical reliability for direct downloads, YouTube, YouTube playlists, Instagram, and Facebook over public-store compliance.

## Technical Direction

- Native Kotlin and Jetpack Compose own the Android experience: UI, navigation, queue management, foreground service, notifications, storage, settings, language switching, and direct URL downloads.
- `yt-dlp` is the core extraction engine for YouTube, playlists, Instagram, and Facebook because these platforms change often and custom extractors would be expensive to maintain.
- `ffmpeg` is the core media processing engine for merge, remux, and format handling when platform downloads produce separate audio/video streams or segmented media.
- Direct file downloads should be handled natively first, with pause/resume, retries, progress, speed, and ETA.
- `aria2c`-style acceleration is optional and should not be part of the MVP unless direct download performance becomes a blocking problem.

## v0.1 - MVP Download Manager

### Goal

Deliver a usable private app that handles direct file downloads, YouTube single videos, and YouTube playlists with a stable queue and clear download status.

### Features

- Paste a URL manually inside the app.
- Receive shared links through Android Share Target.
- Download direct URLs for files such as videos, archives, documents, and other normal HTTP/HTTPS resources.
- Download a single YouTube video through the media engine.
- Expand a YouTube playlist into multiple queued download jobs.
- Show active downloads, queued downloads, completed downloads, and failed downloads.
- Run active downloads through a foreground service with a persistent notification.
- Show basic progress, speed, ETA, and current file name.
- Save files under `Downloads/Girum/` using category-based folders.
- Support Dari as the highest-priority UI language.
- Support switching between Dari RTL and English LTR.

### Exclusions

- Instagram support.
- Facebook support.
- Advanced quality picker.
- Cookie/session import.
- Exact nightly scheduling.
- `aria2c` integration.
- Play Store release requirements.

### Acceptance Criteria

- A user can paste a direct download URL and save the file under `Downloads/Girum/`.
- A user can share a link from another app and Girum receives it.
- A user can download one YouTube video.
- A user can add a YouTube playlist and see it become multiple queue items.
- Active downloads continue while the app is backgrounded, with a visible foreground notification.
- Completed files remain visible to normal file manager or media apps where Android storage rules allow it.
- The UI can switch between Dari RTL and English LTR.

## v0.2 - Platform Expansion

### Goal

Add practical support for Instagram and Facebook while improving media selection and extraction reliability.

### Features

- Download public Instagram reels and posts when supported by the media engine.
- Download public Facebook videos when supported by the media engine.
- Add a quality picker for supported YouTube downloads.
- Improve extraction error messages so users know whether a link is unsupported, private, expired, blocked, or requires login.
- Add optional cookie/session support for trusted users who need access to account-required media.
- Improve download history with platform, title, source URL, output path, status, and failure reason.

### Exclusions

- Guaranteeing every Instagram or Facebook link works.
- Full account login inside the app.
- Downloading private content without user-provided access.
- Automated scraping of whole profiles, pages, or groups.
- Exact nightly scheduling.
- `aria2c` integration unless it is needed for direct download performance.

### Acceptance Criteria

- A user can share or paste a public Instagram reel/post URL and Girum attempts extraction and download.
- A user can share or paste a public Facebook video URL and Girum attempts extraction and download.
- Unsupported or blocked links fail with a clear user-facing reason.
- A user can select a preferred quality for supported YouTube downloads.
- Cookie/session support is optional, documented in-app, and disabled by default.
- Download history is useful enough to retry or inspect failed jobs.

## v0.3 - Automation and Performance

### Goal

Improve the app for heavy personal use: batch downloads, nightly queues, stronger retries, and optional high-performance download acceleration.

### Features

- Add nightly queue scheduling with constraints such as charging and unmetered Wi-Fi.
- Add batch link import from pasted text containing multiple URLs.
- Add stronger retry rules with exponential backoff and resumable downloads where supported.
- Add queue controls such as pause all, resume all, retry failed, reorder, and cancel selected.
- Add optional multi-connection acceleration for direct downloads where the server supports HTTP range requests.
- Evaluate `aria2c` integration only after native direct download behavior is stable.
- Add storage cleanup tools for incomplete downloads and failed temporary files.

### Exclusions

- Perfect exact-time background execution on every Android device.
- Guaranteeing platform downloads continue to work after platform-side breaking changes.
- Play Store publication work.
- Replacing `yt-dlp` with custom platform extractors.

### Acceptance Criteria

- A user can schedule a queue to start during a preferred overnight window with charging or Wi-Fi constraints.
- A user can paste many links at once and Girum creates multiple queue items.
- Failed jobs retry predictably and preserve useful failure details.
- Direct downloads can resume after interruption when the server supports it.
- Optional acceleration does not break normal direct downloads when disabled.
- Temporary files can be cleaned up from inside the app.

## Senior-Dev Warnings

- "Download from any platform" must not be treated as a guarantee. The supported scope is direct URLs, YouTube, YouTube playlists, Instagram, and Facebook.
- YouTube, Instagram, and Facebook can break extraction without warning. Girum should expect media engine updates over time.
- Some links require cookies, login sessions, special headers, or platform access. The app should fail clearly instead of pretending every URL is downloadable.
- Android background execution is device-dependent. Foreground service is required for active downloads, and WorkManager-style scheduling is best-effort.
- Public storage is more complex on newer Android versions. Use Android storage APIs correctly instead of relying on unrestricted filesystem access.
- APK size will grow if bundling Python, `yt-dlp`, `ffmpeg`, and optional native binaries.

## Early Out of Scope

- Play Store release.
- Full in-app browser login.
- Downloading private content without user-provided access.
- DRM-protected media.
- Whole-profile or whole-page scraping.
- Desktop or web versions.
- Cloud sync.

# beyar

Android app workspace for VS Code and Codex.

## Environment

- Android SDK: `/home/ahmad/Android/Sdk`
- Java: `/usr/lib/jvm/java-21-openjdk-amd64`
- Gradle wrapper: Gradle 9.4.1
- Android Gradle Plugin: 9.2.0
- Existing emulator: `Small_Phone`
- Starter app: Kotlin + Jetpack Compose + Material 3

## Common Commands

```bash
./gradlew assembleDebug
./gradlew --offline assembleDebug
emulator -avd Small_Phone -no-boot-anim -gpu host
./gradlew installDebug
adb shell am start -n com.ahmad.girum/.MainActivity
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

From VS Code, run **Terminal: Run Task** and choose:

- `Android: Build Debug`
- `Android: Launch Small_Phone Emulator`
- `Android: Run App`

## Android Creator

Install or refresh the local Android Creator app and reusable template:

```bash
tools/install-android-creator
```

Then launch **Android Creator** from Ubuntu applications, or run:

```bash
android-creator
```

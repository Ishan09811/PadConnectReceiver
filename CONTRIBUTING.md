# Contributing to PadConnectReceiver

Thanks for your interest in contributing to PadConnectReceiver the desktop receiver that turns UDP input from the [PadConnect](https://github.com/Ishan09811/PadConnect) Android app into a virtual gamepad.

## Project Overview

PadConnectReceiver is the **desktop** half of the PadConnect project. It listens for UDP packets from the PadConnect Android app, deserializes controller states, and feeds them into linux(native)/windows(vigem) depending on platform to expose a virtual Xbox 360 (DualShock4 planned) controller to the OS.

```
[ Android Phone ] -- UDP --> [ PadConnectReceiver ] --> [ ViGEm ] --> Game
```

If your change involves the Android client (touch UI, input capture, layout editor), please open your PR against [PadConnect](https://github.com/Ishan09811/PadConnect) instead.

## Tech Stack

- Kotlin Multiplatform (KMP) targeting Desktop (JVM)
- Compose Multiplatform (UI)
- ViGEm (Virtual Gamepad Emulation Framework) only for windows platform
- UDP networking (input receiving)

## Getting Started

### Prerequisites

- JDK (version matching `build.gradle.kts`)
- IntelliJ IDEA or Android Studio with Kotlin Multiplatform plugin
- **Windows 10/11** with [ViGEmBus driver](https://github.com/ViGEm/ViGEmBus/releases) installed, if testing controller emulation
- A device or emulator running [PadConnect](https://github.com/Ishan09811/PadConnect) to test against

### Setup

1. Fork the repository and clone your fork:
   ```
   git clone https://github.com/username/PadConnectReceiver.git
   cd PadConnectReceiver
   ```
2. Build and run the desktop app:

   On Linux:
   ```
   ./gradlew :composeApp:run
   ```
   On Windows:
   ```
   .\gradlew.bat :composeApp:run
   ```

### Testing end to end

To verify input is actually reaching a virtual controller, you will need:
1. ViGEmBus installed and a reboot completed (required only for Windows).
2. The [PadConnect](https://github.com/Ishan09811/PadConnect) Android app running on the same local network.
3. A way to inspect gamepad input (e.g. Windows' "Set up USB game controllers" panel, or a game).

Note that PadConnect and PadConnectReceiver versions are paired so make sure to check the compatibility note in each release's changelog (e.g. a receiver release may require a minimum PadConnect version) before assuming a connection issue is a bug.

## Project Structure

```
composeApp/
 ├─ src/
 │   ├─ jvmMain/        # main codes
gradle/                 # gradle files
```

Within the receiver logic, expect roughly these areas of responsibility:
- **data** — controller state model
- **input** — UDP listening
- **native** — native bindings (depends on the platform)
- **viewmodel / ui** — desktop app UI (Compose)

## How to Contribute

### Reporting Bugs

Open an issue and include:
- OS and version (Windows 10/11, or Linux distro if testing Linux support)
- PadConnectReceiver version
- PadConnect (Android) version
- Whether ViGEmBus is installed and up to date (required only for Windows users)
- Steps to reproduce, and logs/console output if available

### Suggesting Features

Open an issue describing the use case first, especially for anything touching ViGEm/controller emulation as these changes tend to need discussion around platform support (Windows vs the newer Linux support) before implementation.

### Submitting Code Changes

1. Create a branch off `master`:
   ```
   git checkout -b feature/short-description
   ```
2. Make your changes.
3. Test on the actual target platform where possible as ViGEm behavior is Windows specific and can't be fully verified on other platforms.
4. Commit with a clear, descriptive message.
5. Push to your fork and open a pull request against `master`.
6. Describe what changed, why, and how you tested it (including OS used).

### Commit Messages

Keep commits focused and descriptive, e.g.:
```
input: handle malformed UDP packets without crashing
native: fix ViGEm handle leak on disconnect
```

### Code Style

- Follow standard Kotlin conventions.
- Native interop code should be isolated behind a clear interface so it can eventually support additional platforms (see the Linux support roadmap item).

### Pull Request Checklist

- [ ] Code builds and runs (`./gradlew :composeApp:run`)
- [ ] Tested against a real PadConnect Android client where the change affects the input pipeline
- [ ] Windows specific changes tested with ViGEmBus installed
- [ ] No unrelated changes bundled into the PR
- [ ] PR description explains the change, testing performed, and OS used

## License

By contributing, you agree that your contributions will be licensed under the project's [GPL-3.0-only license](LICENSE).

## Questions?

Join the [Discord server](https://discord.gg/BrMAZbEyXs) or open a GitHub issue.

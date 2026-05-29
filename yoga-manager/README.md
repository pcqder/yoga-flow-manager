# Yoga Studio Manager

A console-based Java app for a yoga teacher to manage students and plan classes.

## Run it in IntelliJ (3 steps)

1. **Unzip** this archive anywhere.
2. **IntelliJ IDEA** → `File` → `Open...` → select the `YogaManagerGradle` folder → **Trust Project**.
3. Wait for Gradle to sync (status bar at the bottom). When done, click the green ▶ next to **YogaManager** in the top toolbar.

> No need to install Gradle or a JDK manually — the Gradle wrapper bundled in this project downloads Gradle 8.10 automatically and the build toolchain downloads JDK 17 if you don't have it.

### Alternative: command line

```bash
./gradlew run --console=plain          # macOS / Linux
gradlew.bat run --console=plain        # Windows
```

## Features

- Add / list students with membership type (Monthly, Drop-in, Unlimited)
- Track monthly attendance and payment status
- Schedule classes, enroll / unenroll students, capacity limits
- Monthly revenue report
- Reset monthly data
- Auto-saves to `yoga_data.dat` in the project folder

## Project layout

```
YogaManagerGradle/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat       # Gradle wrapper (no install needed)
├── gradle/wrapper/
├── .idea/runConfigurations/    # Pre-wired Run buttons for IntelliJ
└── src/main/java/com/yoga/
    ├── YogaManager.java        # main()
    ├── Student.java
    └── YogaClass.java
```

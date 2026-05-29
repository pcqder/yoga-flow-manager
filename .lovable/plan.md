## Goal

Make `YogaManagerGradle.zip` fully self-contained: unzip → open folder in IntelliJ → press Run. No extra installs, no manual Gradle setup.

## What I'll do

1. **Rebuild the project folder** at `/tmp/YogaManagerGradle` with the existing Java sources (`YogaManager.java`, `Student.java`, `YogaClass.java`) under `src/main/java/com/yoga/`.

2. **Add the Gradle wrapper** (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`) so IntelliJ can auto-download the right Gradle version on first open — user does not need Gradle installed.

3. **Add a `.idea/` run configuration** (`runConfigurations/YogaManager.xml`) that pre-wires a "Run YogaManager" green play button in IntelliJ, pointing at `com.yoga.YogaManager`'s `main()`. This skips the "configure SDK / pick main class" step.

4. **Keep `build.gradle`** with:
   - `application` plugin
   - `mainClass = 'com.yoga.YogaManager'`
   - Java 17 toolchain (auto-downloads JDK if missing)
   - `run { standardInput = System.in }` so console input works

5. **Update `README.md`** with the simplest possible steps:
   - Unzip
   - IntelliJ → File → Open → select the `YogaManager` folder → Trust Project
   - Wait for Gradle sync
   - Click the green ▶ next to "YogaManager" in the toolbar (or run `YogaManager.java`)

6. **Verify** by compiling the sources and running the wrapper task list to confirm the zip is valid, then produce `YogaManagerGradle.zip` in `/mnt/documents/`.

## Result

One zip. Unzip → Open in IntelliJ → Run. Nothing else.

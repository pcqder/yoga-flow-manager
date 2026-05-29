# Yoga Studio Manager (Web App)

A simple Java web application built with Gradle + Javalin to manage yoga
students and plan classes — all in your browser.

## Requirements

- JDK 17 or newer (works on JDK 21 / 26)
- IntelliJ IDEA (Community is fine)

## Run from IntelliJ

1. **File → Open** → select the `yoga-manager` folder → **Trust Project**
2. Wait for the Gradle sync to finish
3. Open the Gradle tool window → `yoga-manager → Tasks → application → run` (double-click)
4. Open <http://localhost:7070> in your browser

Alternatively run from a terminal inside the project:

```bash
./gradlew run          # macOS / Linux
gradlew.bat run        # Windows
```

Data is persisted to `yoga_data.dat` next to the working directory.

## Features

- Dashboard with monthly revenue / attendance stats
- Add, edit, remove students; mark payment & attendance
- Schedule classes with date, time, location, capacity
- Enroll students into classes
- Reset month to start a new billing cycle

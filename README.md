# EducationalApp

Study app prototype for tracking focus sessions, goals, reviews, and progress.

## Backend

Path:

```text
backend/
```

Run locally:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

API base URL:

```text
http://localhost:8080
```

Demo login:

```json
{
  "email": "demo@educational.app",
  "password": "123456"
}
```

H2 console:

```text
http://localhost:8080/h2-console
```

H2 settings:

```text
JDBC URL: jdbc:h2:mem:educationalapp
User: sa
Password:
```

## Android Frontend

Path:

```text
EducationalApplication/
```

Open this folder in Android Studio:

```text
EducationalApplication/
```

The Android emulator calls the backend at:

```text
http://10.0.2.2:8080
```

Before using login in the app, start the backend locally.

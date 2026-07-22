# Firebase Storage Spring Handler

[🇧🇷 Ler em Português](README.pt-BR.md)

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-blue?logo=postgresql)
![Maven](https://img.shields.io/badge/build-Maven-red?logo=apachemaven)
![Firebase](https://img.shields.io/badge/Firebase-Storage-yellow?logo=firebase)

A Spring Boot REST API that uploads images to **Firebase Cloud Storage** and persists their metadata in **PostgreSQL**, so files can later be retrieved either by their storage filename or by their database id.

## Features

- Upload images to Firebase Storage with server-side validation
  - Max file size: **5MB**
  - Allowed types: `image/jpeg`, `image/png`, `image/webp`, `image/jpg`, `image/gif`
  - MIME type detected from the file's actual bytes (via Apache Tika), not just the filename extension
- Download an image by its storage filename
- Download an image by its database id
- Image metadata (original filename, storage filename, size, MIME type) persisted in PostgreSQL
- Centralized JSON error responses for common failure cases

## Tech Stack

- Java 17
- Spring Boot 4 (Web, Data JPA, Validation)
- PostgreSQL
- Firebase Admin SDK
- Apache Tika (file type detection)
- Maven (with wrapper)
- Docker

## Prerequisites

- JDK 17+
- Maven (or use the included `./mvnw` wrapper — no local Maven install required)
- A running PostgreSQL instance
- A Firebase project with Cloud Storage enabled

## Firebase Setup

1. **Create a Firebase project** at the [Firebase Console](https://console.firebase.google.com/) (or use an existing one).
2. **Enable Cloud Storage**:
   - In the left sidebar, go to **Build → Storage**.
   - Click **Get started** and follow the prompts to provision a bucket (choose a location and start rules).
3. **Generate a service account credentials file**:
   - Go to **Project Settings → Service accounts**.
   - Click **Generate new private key**. This downloads a JSON file containing your service account credentials.
4. **Place the credentials file in the project**:
   - Save the downloaded JSON as:
     ```
     src/main/resources/firebase-service-account.json
     ```
   - This path is already listed in `.gitignore` — **never commit this file**, it grants full access to your Firebase project.
5. **Copy your storage bucket name**:
   - In the Storage console, note the bucket name (e.g. `your-project.firebasestorage.app`). You'll need it for the `.env` file below.

## Environment Variables (`.env`)

Copy the example file and fill in your own values:

```bash
cp .env.example .env
```

| Variable | Description | Example |
|---|---|---|
| `DATABASE_URL` | Postgres host, port and database name (no `jdbc:postgresql://` prefix — that's added automatically) | `localhost:5432/images` |
| `DATABASE_USERNAME` | Postgres username | `postgres` |
| `DATABASE_PASSWORD` | Postgres password | `your-db-pass` |
| `FIREBASE_CONFIG_PATH` | Classpath location of the service account JSON | `classpath:firebase-service-account.json` |
| `FIREBASE_PROJECT_ID` | Your Firebase project id | `your-firebase-proj-id` |
| `FIREBASE_STORAGE_BUCKET` | Your Firebase Storage bucket name | `your-project.firebasestorage.app` |
| `PORT` | Port the app listens on | `8080` |

The application reads `.env` automatically at startup (via Spring's `spring.config.import`) — no extra library or manual export needed.

## Database Setup

The app connects to PostgreSQL using `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` from your `.env`. The schema (the `images` table) is created and updated automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`) — there's no manual migration step.

**Option A — local PostgreSQL install:**

```bash
createdb images
```

Then point `DATABASE_URL` at `localhost:5432/images` (adjust host/port as needed).

**Option B — PostgreSQL via Docker:**

```bash
docker run --name images-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your-db-pass \
  -e POSTGRES_DB=images \
  -p 5432:5432 \
  -d postgres
```

## Running the App

**With Maven wrapper:**

```bash
./mvnw spring-boot:run
```

**Build and run a jar:**

```bash
./mvnw clean package
java -jar target/*.jar
```

**With Docker:**

The Docker image only bundles the compiled jar, so the Firebase credentials file must be present in the build context before building (it's gitignored, so add it locally first) or mounted at runtime.

```bash
docker build -t firebase-storage-spring-handler .
docker run -p 8080:8080 --env-file .env firebase-storage-spring-handler
```

The app starts on `http://localhost:8080` by default (configurable via `PORT`).

## API Reference

Base path: `/images`

| Method | Path | Description | Request | Response |
|---|---|---|---|---|
| `POST` | `/images/upload` | Uploads an image and stores its metadata | Multipart form field `file` | `200` — `ImageMetadataResponseDTO` (`id`, `storageFileName`, `originalFileName`, `size`, `mimeType`) |
| `GET` | `/images/{fileName}` | Downloads an image by its storage filename | Path param `fileName` (e.g. `<uuid>.png`) | `200` — raw image bytes with correct `Content-Type`; `404` if not found |
| `GET` | `/images/id/{imageMetadataId}` | Downloads an image by its metadata id | Path param `imageMetadataId` | `200` — raw image bytes with correct `Content-Type`; `404` if not found |

All errors are returned as JSON in the shape:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "...",
  "message": "..."
}
```

## Project Structure

```
src/main/java/com/springboot/firebaseStorage/
├── controller/        # REST controllers (ImageController)
├── service/            # Business logic (ImageService)
├── repository/         # Spring Data JPA repositories
├── model/               # JPA entities and DTOs
├── infra/firebase/     # Firebase initialization and upload validation
└── exceptions/         # Custom exceptions
```

## Author

**Vinícius Muller**
- GitHub: [@Viinicius-Muller](https://github.com/Viinicius-Muller)
- Email: zandreviniciusmuller@gmail.com

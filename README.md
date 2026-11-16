# Google Drive Clone

A Google Drive–style file storage platform built as 5 independent Java/Spring Boot microservices, tied together with service discovery, an API gateway, and a circuit breaker for resilience.

## Architecture

```
                 ┌─────────────────┐
  Client/Postman │   API Gateway    │  :8080
                 └────────┬─────────┘
          ┌───────────────┼───────────────┐
          ▼                ▼               ▼
  ┌───────────────┐ ┌──────────────┐ ┌──────────────┐
  │ File-Services  │ │folder-services│ │Search-Services│
  │    :8081       │ │    :8082      │ │    :8083      │
  └───────┬────────┘ └──────┬───────┘ └──────────────┘
          │                 │                  ▲
          ▼                 ▼                  │
  ┌─────────────────────────────────┐          │
  │     MySQL (filedb, folderdb)    │          │
  └─────────────────────────────────┘          │
                                                │
          All 5 services register with ────────┘
          Eureka (service discovery) :8761
```

- **Eureka** — service registry. Every service registers itself on startup; the gateway and Search-Services look services up by name instead of a hardcoded address.
- **API Gateway** — the single entry point. Routes requests by path to the right service.
- **File-Services** — file upload/download and metadata, backed by MySQL.
- **folder-services** — folder hierarchy (parent/child), backed by MySQL.
- **Search-Services** — no database of its own; calls File-Services and folder-services and merges results, with a Resilience4j circuit breaker protecting each call.

## Tech stack

Java 21 · Spring Boot · Spring Cloud (Gateway, Eureka, OpenFeign) · Resilience4j · MySQL · Docker & Docker Compose

## Running it

**Option A — Docker Compose (recommended, one command):**

```
cp .env.example .env     # then edit .env and set your real DB password
docker-compose up --build
```

This builds and starts all 5 services plus MySQL together.

**Option B — run manually:**

Start in this order so each service can register with Eureka: Eureka → MySQL → File-Services / folder-services → Search-Services → API Gateway. Each service's own `pom.xml`/`application.properties` has what it needs.

Once running, everything is reachable through the gateway at `http://localhost:8080`.

## API overview

All requests go through the gateway on port 8080.

### Files — `/api/files`
| Method | Path | Description |
|---|---|---|
| GET | `/api/files` | list all files |
| GET | `/api/files/{id}` | get one file's metadata |
| POST | `/api/files` | create file metadata (JSON body) |
| POST | `/api/files/upload` | upload a real file (multipart form-data: `name`, `folderId`, `file`) |
| GET | `/api/files/download/{id}` | download the file's bytes |
| DELETE | `/api/files/{id}` | delete a file |
| GET | `/api/files/folder/{folderId}` | list files in a folder |

### Folders — `/api/folders`
| Method | Path | Description |
|---|---|---|
| GET | `/api/folders` | list all folders |
| GET | `/api/folders/{id}` | get one folder |
| POST | `/api/folders` | create a folder (JSON body: `name`, `parentId`) |
| DELETE | `/api/folders/{id}` | delete a folder |
| GET | `/api/folders/parent/{parentId}` | list folders under a parent |

### Search — `/api/search`
| Method | Path | Description |
|---|---|---|
| GET | `/api/search?query=` | search files and folders together |
| GET | `/api/search/files?query=` | search files only |
| GET | `/api/search/folders?query=` | search folders only |

## Known limitations

This project intentionally focuses on service discovery, routing, and resilience rather than every production concern:

- No authentication/authorization — any request to the gateway is accepted as-is.
- No cascading delete — deleting a folder does not delete its sub-folders or the files inside it.
- No file-type or size validation beyond a basic upload size limit.
- No service/DTO layer — controllers talk directly to the database layer.

## What's been hardened

- Fixed gateway routes that pointed at non-existent services.
- Fixed a path-traversal / insecure-direct-object-reference vulnerability in file upload and download.
- Enabled and correctly wired the Resilience4j circuit breaker for both downstream calls in Search-Services.
- Added consistent error handling and correct HTTP status codes across all services.
- Migrated from in-memory H2 to a persistent MySQL database.
- Externalized database credentials to environment variables instead of committing them.

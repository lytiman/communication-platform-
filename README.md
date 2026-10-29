# Communication Platform

A real-time team communication backend inspired by Slack and Discord. Users can organize conversations into **groups** and **channels**, exchange **messages** and **private chats**, receive **notifications**, share **files**, and view other users' **online presence** in real time.

Built with **Spring Boot 3**, **PostgreSQL**, **JWT authentication**, and **WebSocket (STOMP)**.

!\[Java](https://img.shields.io/badge/Java-21-orange)
!\[Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
!\[PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
!\[Auth](https://img.shields.io/badge/Auth-JWT-purple)

\---

## Table of Contents

* [Features](#features)
* [Tech Stack](#tech-stack)
* [Architecture](#architecture)
* [Project Structure](#project-structure)
* [Getting Started](#getting-started)
* [Configuration](#configuration)
* [Authentication Flow](#authentication-flow)
* [API Reference](#api-reference)
* [Real-Time (WebSocket)](#real-time-websocket)
* [Roles and Permissions](#roles-and-permissions)
* [Roadmap and Known Limitations](#roadmap-and-known-limitations)



## Features

* **Authentication**: register, login, refresh-token rotation, per-device logout, and "log out everywhere"
* **Groups**: create groups, manage members, and assign group-level roles (Owner / Admin / Member)
* **Channels**: public or private channels inside groups, with invite codes that can be regenerated
* **Messaging**: text, image, video, file, audio, and system messages, with replies, edits, and soft deletes
* **Private chats**: one-to-one conversations with unread counts and read receipts
* **Notifications**: new messages, invites, mentions, and role changes
* **File uploads**: category-based storage with server-generated UUID filenames
* **Presence**: online/offline status and last-seen time, broadcast in real time
* **Admin tools**: list, disable, ban, and manage roles for users
* **Pagination**: message history is paged, never loaded all at once
* **Consistent errors**: one global exception handler returns clean JSON for every failure

## Tech Stack

|Area|Technology|
|-|-|
|Language|Java 21|
|Framework|Spring Boot 3.5 (Web, Data JPA, Validation, Security, WebSocket)|
|Database|PostgreSQL 16 (via Docker Compose)|
|Auth|Spring Security + JWT ([jjwt](https://github.com/jwtk/jjwt) 0.12.6)|
|Real-time|STOMP over WebSocket with SockJS fallback|
|Boilerplate|Lombok|
|Build|Maven (wrapper included)|

## Architecture

The code follows a classic layered design. Each layer talks only to the one below it.

```
Client (REST + WebSocket)
        │
        ▼
 ┌──────────────┐   HTTP endpoints, request validation
 │  Controller  │
 └──────┬───────┘
        ▼
 ┌──────────────┐   business rules, permissions, transactions
 │   Service    │──────► pushes events to WebSocket topics
 └──────┬───────┘
        ▼
 ┌──────────────┐   Spring Data JPA interfaces
 │  Repository  │
 └──────┬───────┘
        ▼
   PostgreSQL
```

**Design decisions**

* **REST to send, WebSocket to receive.** Clients send messages through REST endpoints, where validation and permission checks live. WebSocket is used only to push updates to subscribers instantly.
* **Two kinds of tokens.** Access tokens are short-lived, stateless JWTs. Refresh tokens are opaque random strings stored in the database, so they can be revoked per device.
* **DTOs everywhere.** Entities are never exposed directly, so fields like `password` can't leak.
* **Soft deletes for messages.** Rows stay in the database and content is blanked in responses.

## Project Structure

```
src/main/java/com/maalfa/communication\_platform/
├── config/        # CORS, seeding, mapper and OpenAPI config
├── controller/    # REST endpoints
├── dto/           # Request/response objects, grouped by feature
├── entity/        # JPA entities (User, Group, Channel, Message, ...)
├── enums/         # Roles, message types/status, notification types, presence
├── exception/     # Custom exceptions + GlobalExceptionHandler
├── mapper/        # Entity <-> DTO conversion
├── repository/    # Spring Data JPA repositories
├── security/      # JWT service/filter, SecurityConfig, UserPrincipal, permissions
├── service/       # Service interfaces
│   └── impl/      # Business logic implementations
├── util/          # SecurityUtil (current user lookup), helpers
└── websocket/     # STOMP config, auth interceptor, presence listener
```

## Getting Started

### Prerequisites

* Java 21
* Docker and Docker Compose
* (Optional) Postman or curl for testing

### 1\. Clone the repository

```bash
git clone https://github.com/<your-username>/communication\_platform.git
cd communication\_platform
```

### 2\. Start PostgreSQL

```bash
docker compose up -d
```

This starts PostgreSQL 16 on port `5432` and pgAdmin on port `5050`. The credentials are configured in `docker-compose.yml`. **Change the default credentials before using this setup outside local development.**

### 3\. Run the application

```bash
./mvnw spring-boot:run
```

On Windows, run: `mvnw.cmd spring-boot:run`

The API is available at **http://localhost:8077**. On first startup, `DataSeeder` creates the default roles, and Hibernate creates the database tables automatically.

### 4\. Try it out

Register:

```bash
curl -X POST http://localhost:8077/api/auth/register \\
  -H "Content-Type: application/json" \\
  -d '{"username":"alice","email":"alice@example.com","password":"password123","fullName":"Alice"}'
```

Login:

```bash
curl -X POST http://localhost:8077/api/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{"usernameOrEmail":"alice","password":"password123"}'
```

Use the returned `accessToken` on protected endpoints:

```bash
curl http://localhost:8077/api/users/me \\
  -H "Authorization: Bearer <accessToken>"
```

## Configuration

Settings live in `src/main/resources/application.properties`.

|Property|Purpose|Default|
|-|-|-|
|`server.port`|HTTP port|`8077`|
|`spring.datasource.url`|Database JDBC URL|`jdbc:postgresql://localhost:5432/communication\_platform`|
|`spring.datasource.username` / `password`|Database credentials|see file|
|`app.jwt.secret`|HMAC signing key, **at least 32 bytes**|placeholder, **must change**|
|`app.jwt.access-token-expiration-ms`|Access token lifetime|`900000` (15 min)|
|`app.jwt.refresh-token-expiration-ms`|Refresh token lifetime|`2592000000` (30 days)|
|`app.file.upload-dir`|Where uploaded files are stored|`./uploads`|
|`app.file.base-url`|URL prefix for serving files|`/api/files`|

> \*\*Never commit real secrets to version control.\*\* Spring Boot supports environment variables, allowing you to override configuration properties without editing the file:
>
> ```bash
> export APP\_JWT\_SECRET="a-long-random-string-of-at-least-32-bytes"
> export SPRING\_DATASOURCE\_PASSWORD="your-db-password"
> ```

CORS is configured for a frontend running at `http://localhost:5173`. Update `CorsConfig` to allow additional origins when needed.

## Authentication Flow

```
1. POST /api/auth/register  or  /api/auth/login
      └─► { accessToken (JWT, 15 min), refreshToken (UUID, 30 days), user }

2. Every protected request:
      Authorization: Bearer <accessToken>

3. When the access token expires:
      POST /api/auth/refresh { refreshToken }
      └─► new accessToken + NEW refreshToken (the old one is deleted)

4. POST /api/auth/logout { refreshToken }  → ends one device's session
   POST /api/users/me/logout-all           → ends every session
```

Refresh tokens are **rotated** on each use, so a stolen token can't be replayed indefinitely.

## API Reference

All endpoints require a valid `Authorization: Bearer <token>` header, except endpoints under `/api/auth/\*\*` and public file retrieval endpoints using `GET /api/files/\*\*`.

### Auth, `/api/auth`

|Method|Path|Description|
|-|-|-|
|POST|`/register`|Create an account|
|POST|`/login`|Log in with username or email|
|POST|`/refresh`|Exchange a refresh token for new tokens|
|POST|`/logout`|Revoke one refresh token|

### Users, `/api/users`

|Method|Path|Description|
|-|-|-|
|GET|`/me`|Current user's profile|
|PATCH|`/me`|Update profile|
|GET|`/{userId}`|View a user|
|GET|`/search`|Search by username or display name|
|POST|`/me/logout-all`|Log out of all devices|

### Groups, `/api/groups`

|Method|Path|Description|
|-|-|-|
|POST|`/`|Create a group|
|GET|`/my`|Groups I belong to|
|GET|`/{groupId}`|Group details|
|GET|`/{groupId}/members`|List members|
|POST|`/{groupId}/members`|Add a member|
|DELETE|`/{groupId}/members/{userId}`|Remove a member|
|POST|`/{groupId}/leave`|Leave a group|
|DELETE|`/{groupId}`|Delete a group|

### Channels, `/api/channels`

|Method|Path|Description|
|-|-|-|
|POST|`/`|Create a channel in a group|
|GET|`/{channelId}`|Channel details|
|GET|`/group/{groupId}`|Channels in a group|
|GET|`/{channelId}/members`|List members|
|PATCH|`/{channelId}`|Update a channel|
|DELETE|`/{channelId}`|Delete a channel|
|POST|`/join`|Join using an invite code|
|POST|`/{channelId}/leave`|Leave a channel|
|DELETE|`/{channelId}/members/{userId}`|Remove a member|
|POST|`/{channelId}/invite-code/regenerate`|Invalidate and replace the invite code|

### Messages, `/api/messages`

|Method|Path|Description|
|-|-|-|
|POST|`/`|Send a message to a channel|
|GET|`/{messageId}`|Get one message|
|GET|`/channel/{channelId}?page=0\&size=50`|Paginated channel history|
|PATCH|`/{messageId}`|Edit your message|
|DELETE|`/{messageId}`|Delete (sender or moderator)|

Example body for `POST /api/messages`:

```json
{
  "channelId": 1,
  "content": "Hello team!",
  "type": "TEXT",
  "replyToId": null
}
```

### Private Chats, `/api/chats`

|Method|Path|Description|
|-|-|-|
|GET|`/`|My chat list with previews and unread counts|
|POST|`/messages`|Send a private message|
|GET|`/{chatId}/messages`|Paginated chat history|
|POST|`/{chatId}/read`|Mark a chat as read|
|DELETE|`/messages/{messageId}`|Delete a private message|

### Notifications, `/api/notifications`

|Method|Path|Description|
|-|-|-|
|GET|`/`|All notifications|
|GET|`/unread`|Unread notifications|
|GET|`/unread/count`|Unread count|
|POST|`/read`|Mark specific notifications as read|
|POST|`/read-all`|Mark everything as read|

### Files, `/api/files`

|Method|Path|Description|
|-|-|-|
|POST|`/upload/{category}`|Upload a file (authenticated)|
|GET|`/{category}/{filename}`|Download or display a file (public)|

### Admin, `/api/admin`

|Method|Path|Description|
|-|-|-|
|GET|`/users`|List users|
|POST|`/users/{userId}/disable` / `enable`|Disable or enable an account|
|POST|`/users/{userId}/ban` / `unban`|Ban or unban a user|
|POST|`/users/{userId}/roles/{roleName}`|Grant a role|
|DELETE|`/users/{userId}/roles/{roleName}`|Revoke a role|

## Real-Time (WebSocket)

Connect to `/ws` using SockJS and STOMP. Include your access token in the STOMP `CONNECT` frame:

```
Authorization: Bearer <accessToken>
```

**Subscribe to:**

|Destination|Receives|
|-|-|
|`/topic/channel.{channelId}`|New, edited, and deleted messages in a channel|
|`/topic/presence`|Users going online or offline|
|`/user/queue/messages`|Your incoming private messages|
|`/user/queue/notifications`|Your new notifications|

Example using `@stomp/stompjs` in a browser client:

```js
const client = new Client({
  webSocketFactory: () => new SockJS("http://localhost:8077/ws"),
  connectHeaders: { Authorization: `Bearer ${accessToken}` },
  onConnect: () => {
    client.subscribe("/topic/channel.1", (frame) => {
      console.log(JSON.parse(frame.body));
    });
  },
});
client.activate();
```

## Roles and Permissions

There are two independent permission systems:

**Global roles** (platform-wide)

|Role|Capabilities|
|-|-|
|`ROLE\_USER`|Default for every new account|
|`ROLE\_MODERATOR`|Delete any message, moderate channels|
|`ROLE\_ADMIN`|Manage and ban users, manage channels and groups, view audit log|

**Scoped roles** (per group or channel)

* Group: `OWNER`, `ADMIN`, `MEMBER`
* Channel: `ADMIN`, `MODERATOR`, `MEMBER`

A user can be an owner in one group and a plain member in another.

## Roadmap and Known Limitations

This project is under active development. Address the following items before deploying it to production:

* \[ ] Move all secrets (JWT key, DB credentials) to environment variables or a secret manager
* \[ ] Replace `ddl-auto=update` with **Flyway** migrations (including the DB-level `CHECK` constraint on messages)
* \[ ] Enforce membership checks when subscribing to `/topic/channel.\*`
* \[ ] Reject WebSocket `CONNECT` frames with missing or invalid tokens
* \[ ] Restrict WebSocket allowed origins (currently `\*`) and tighten CORS for production
* \[ ] Add unit tests (Mockito) and integration tests (`@SpringBootTest`)
* \[ ] Add OpenAPI/Swagger documentation
* \[ ] Add per-chat presence to suppress notifications for the chat a user is viewing
* \[ ] Clean up duplicate classes (`PermissionConstants`, `Role`, `Privilege`) and the commented-out `config/SecurityConfig`

\---

**Author:** Latera Mengistu


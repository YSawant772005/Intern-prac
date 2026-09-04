# Phonebook Application

A complete training assignment built with Vue 3, FastAPI, SQLAlchemy, PostgreSQL, Docker, and Docker Compose.

## Project Overview

This application lets a user view all contacts, view one contact, add a new contact, update an existing contact, and delete a contact. The frontend talks to the FastAPI backend over REST, and the backend talks to PostgreSQL through SQLAlchemy ORM.

## Tech Stack

- Vue 3 for the frontend UI
- Vite for local development and frontend bundling
- FastAPI for the REST API
- Python for the backend runtime
- SQLAlchemy ORM for database access
- PostgreSQL for persistent contact storage
- Docker for containerizing each service
- Docker Compose for running frontend, backend, and database together

## Architecture

```mermaid
flowchart TB
  U[User] --> F[Vue 3 Frontend\nVite]
  F -->|REST API| B[FastAPI Backend\nSQLAlchemy]
  B -->|ORM| D[(PostgreSQL)]
```

## Prerequisites

- Docker and Docker Compose installed
- A copy of this repository
- No local PostgreSQL installation is required because Docker Compose starts the database container

For the complete Windows installation and startup procedure, see [RUNNING_THE_PROJECT.md](RUNNING_THE_PROJECT.md).

## Environment Setup

1. Copy `.env.example` to `.env`.
2. Review the generated values.
3. Keep the database credentials and `DATABASE_URL` aligned.

Example:

```env
POSTGRES_DB=phonebook_db
POSTGRES_USER=phonebook_user
POSTGRES_PASSWORD=phonebook_password
DATABASE_URL=postgresql://phonebook_user:phonebook_password@database:5432/phonebook_db
CORS_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
VITE_API_BASE_URL=http://localhost:8000
```

## Running with Docker

Start the application with:

```bash
docker compose up --build
```

Stop it with `Ctrl+C` in the terminal, then run `docker compose down` if you want to remove the containers.

## Accessing the Application

- Frontend: http://localhost:5173
- Backend API: http://localhost:8000
- FastAPI docs: http://localhost:8000/docs
- OpenAPI schema: http://localhost:8000/openapi.json

## API Endpoints

- `GET /contacts` returns all contacts.
- `POST /contacts` creates a new contact.
- `GET /contacts/{id}` returns one contact by ID.
- `PUT /contacts/{id}` updates a contact by ID.
- `DELETE /contacts/{id}` deletes a contact by ID.

## Testing

After starting the stack:

1. Open the frontend and create a contact.
2. Confirm it appears in the contact list and in the detail view.
3. Edit the contact and verify the updated values are shown.
4. Delete the contact and confirm it disappears from the UI and database.
5. Use the FastAPI docs to try the API endpoints directly.

The app also handles empty state, loading state, duplicate phone number, duplicate email, invalid phone number, missing required fields, and nonexistent contact IDs. Contact validation requires exactly 10 phone digits, rejects numbers in names, and accepts standard email addresses such as `name@gmail.com`. The current contact model does not store a separate country field or apply country-specific calling-code validation; phone numbers are treated as local 10-digit numbers.

## Project Structure

- `backend/` contains the FastAPI app, SQLAlchemy models, CRUD functions, and Dockerfile.
- `frontend/` contains the Vue app, router, views, reusable UI components, and Dockerfile.
- `docker-compose.yml` starts the three services together.
- `.env.example` documents the required environment variables.
- `README.md` explains how to run and test the project.
- `RUNNING_THE_PROJECT.md` provides the detailed Windows and Docker setup guide.
- `SYSTEM_GUIDE.md` explains the application code, Docker workflow, and GitHub workflow in simple words.

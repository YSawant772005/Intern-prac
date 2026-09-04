# Running the Phonebook Project

This guide explains how to run the Phonebook application on Windows with Docker Desktop.

The project has three Docker services:

- `database`: PostgreSQL
- `backend`: FastAPI API
- `frontend`: Vue/Vite website

You do not need to install PostgreSQL, Python, or Node.js on Windows when using the Docker method.

## 1. Install the prerequisites

Install the following:

1. **Docker Desktop for Windows**
   - Download it from https://www.docker.com/products/docker-desktop/
   - During installation, use the recommended WSL 2 option if Docker Desktop offers it.
   - Restart Windows if the installer asks you to.
   - Open Docker Desktop and wait until it says Docker is running.

2. **Git** (only needed if you need to download the repository)
   - Download it from https://git-scm.com/download/win

3. **A browser**, such as Chrome, Edge, or Firefox.

## 2. Open the project folder

Open PowerShell in VS Code or open PowerShell separately and run:

```powershell
cd C:\Prac-intern\Intern-prac
```

Confirm that this is the correct folder:

```powershell
Get-ChildItem
```

You should see `docker-compose.yml`, `backend`, `frontend`, and `.env`.

## 3. Check the environment file

The project needs a file named `.env` in the project root, next to `docker-compose.yml`.

Check that it exists:

```powershell
Test-Path .env
```

The command should return `True`.

If it returns `False`, create it from the example file:

```powershell
Copy-Item .env.example .env
```

Do not run that copy command if `.env` already exists, because it would replace your current environment settings.

The important values are:

```env
POSTGRES_DB=phonebook_db
POSTGRES_USER=phonebook_user
POSTGRES_PASSWORD=phonebook_password
DATABASE_URL=postgresql://phonebook_user:phonebook_password@database:5432/phonebook_db
CORS_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
VITE_API_BASE_URL=http://localhost:8000
```

`DATABASE_URL` uses the hostname `database` because the backend connects to PostgreSQL inside the Docker Compose network. Do not change it to `localhost` for the Docker setup.

## 4. Confirm Docker is available

In the same PowerShell terminal, run:

```powershell
docker --version
docker compose version
docker info
```

The first two commands should print version information. `docker info` should show Docker server details. If it reports that it cannot connect to the Docker daemon, open Docker Desktop and wait for it to finish starting.

## 5. Validate the Compose configuration

Run:

```powershell
docker compose config
```

This checks that the Compose file and `.env` values can be read. If it prints the expanded configuration without an error, continue.

## 6. Start the application

Only **one terminal is required** for the normal startup.

Run this from `C:\Prac-intern\Intern-prac`:

```powershell
docker compose up --build
```

The first run may take several minutes because Docker downloads the PostgreSQL, Python, and Node images and installs dependencies.

Keep this terminal open while using the application. The startup is complete when the logs show the frontend, backend, and database running without a fatal error.

The backend automatically creates the contacts table when it starts. No separate database initialization command is required.

## Contact validation rules

- Phone numbers must contain exactly 10 digits. Spaces, parentheses, dots, and hyphens are allowed as formatting characters.
- Names are required and cannot contain numbers.
- Email is optional, but when provided it must use a standard email format, such as `name@gmail.com`.
- The current contact model does not store a separate country field or validate country-specific calling codes. Numbers are treated as local 10-digit numbers, so the form example uses an Indian-style 10-digit number without the `+91` country prefix.

## 7. Open the application

Open these URLs in a browser:

- Website: http://localhost:5173
- Backend API: http://localhost:8000
- FastAPI interactive documentation: http://localhost:8000/docs

To verify the full setup, create a contact in the website, refresh the page, edit it, and delete it.

## 8. Optional second terminal

A second PowerShell terminal is not required, but it is useful for checks while the first terminal is running `docker compose up --build`.

Open a second terminal and run:

```powershell
cd C:\Prac-intern\Intern-prac
docker compose ps
```

All three services should be running. To view logs without stopping the first terminal:

```powershell
docker compose logs -f backend
```

Press `Ctrl+C` to stop viewing logs. This does not stop the containers.

To check the backend from PowerShell:

```powershell
Invoke-WebRequest http://localhost:8000/docs
```

A successful response has status code `200`.

## 9. Stop the application

In the terminal running Compose, press:

```text
Ctrl+C
```

Then remove the stopped containers with:

```powershell
docker compose down
```

The PostgreSQL data volume is preserved, so contacts remain available the next time you start the project.

## 10. Start it again later

After the first successful build, use:

```powershell
cd C:\Prac-intern\Intern-prac
docker compose up
```

Use `docker compose up --build` again only when you change a Dockerfile, dependency file, or need to rebuild an image.

## 11. Reset the database completely

Warning: this permanently deletes all contacts stored by this project.

```powershell
docker compose down -v
docker compose up --build
```

The `-v` removes the `postgres_data` volume. PostgreSQL will then start with an empty database.

## 12. Common problems

### `docker is not recognized`

Docker Desktop or its command-line tools are not installed correctly. Install Docker Desktop, restart Windows if requested, then open a new PowerShell window.

### `Cannot connect to the Docker daemon`

Docker Desktop is not running yet. Open it, wait until it reports that Docker is running, and retry the command.

### `required variable ... is not set` or `.env` errors

Make sure PowerShell is in `C:\Prac-intern\Intern-prac` and that `.env` exists:

```powershell
cd C:\Prac-intern\Intern-prac
Test-Path .env
docker compose config
```

### Port `5432`, `8000`, or `5173` is already allocated

Another application is using the port. Stop that application, or close another copy of this project before running Compose again. You can inspect common port users with:

```powershell
Get-NetTCPConnection -LocalPort 5432,8000,5173 -ErrorAction SilentlyContinue
```

### The frontend opens but contacts do not load

Check the backend logs in the second terminal:

```powershell
docker compose logs backend
```

Also confirm that http://localhost:8000/docs opens. If the database failed to start, inspect all services:

```powershell
docker compose logs database backend
```

### Changes are not visible

Stop Compose with `Ctrl+C`, then rebuild:

```powershell
docker compose down
docker compose up --build
```

## 13. Useful Docker commands

```powershell
# List running services
docker compose ps

# Follow all service logs
docker compose logs -f

# Restart the services
docker compose restart

# Stop and remove containers, preserving database data
docker compose down

# Show downloaded Docker images
docker images
```

## Terminal summary

For the normal workflow, use one PowerShell terminal:

```powershell
cd C:\Prac-intern\Intern-prac
docker compose up --build
```

Keep it open, then browse to http://localhost:5173. Use a second terminal only for optional logs or health checks.

# Phonebook Application: Simple System Guide

This document explains how the Phonebook application works, what each important file does, how Docker runs it, and how to move the project to GitHub and manage future changes.

## 1. What this project is

This is a phonebook application. A user can:

- See all saved contacts.
- Open one contact.
- Add a contact.
- Edit a contact.
- Delete a contact.

The application has three parts:

1. **Frontend:** Vue 3. This is the website that the user sees in the browser.
2. **Backend:** FastAPI and Python. This receives requests from the website and applies the rules.
3. **Database:** PostgreSQL. This permanently stores the contacts.

The frontend does not talk directly to PostgreSQL. The request always follows this path:

```text
Browser -> Vue frontend -> FastAPI backend -> SQLAlchemy -> PostgreSQL
```

For a response, the path goes back in the opposite direction:

```text
PostgreSQL -> SQLAlchemy -> FastAPI -> Vue frontend -> Browser
```

## 2. How one contact request works

### Loading the contact list

1. You open `http://localhost:5173`.
2. Vue loads `ContactListView.vue`.
3. When the page is mounted, it calls `listContacts()`.
4. `api.js` sends `GET http://localhost:8000/contacts`.
5. FastAPI receives the request in `routes/contacts.py`.
6. The route calls `crud.get_contacts()`.
7. SQLAlchemy reads rows from PostgreSQL.
8. FastAPI returns JSON.
9. Vue places the contacts into the page and `ContactCard.vue` displays each one.

### Creating a contact

1. You fill in the form in `ContactForm.vue`.
2. The frontend checks the name, phone number, and email before sending.
3. `ContactFormView.vue` calls `createContact()` from `api.js`.
4. The backend receives `POST /contacts`.
5. `schemas.py` validates the request again. Backend validation is important because users can bypass browser validation.
6. `crud.py` checks that the phone number and email are not already used.
7. SQLAlchemy inserts the new row into PostgreSQL.
8. The backend returns the new contact.
9. Vue redirects to the contact details page.

### Editing and deleting

Editing uses `PUT /contacts/{id}` and follows the same validation and duplicate checks as creation.

Deleting uses `DELETE /contacts/{id}`. The backend finds the contact, deletes it, commits the transaction, and the frontend returns to the list.

## 3. Validation rules

New and updated contacts must satisfy these rules:

- Name is required.
- Name cannot be longer than 255 characters.
- Name cannot contain numbers.
- Phone number is required.
- Phone number must contain exactly 10 digits.
- Phone formatting characters such as spaces, `+`, parentheses, dots, and hyphens are allowed.
- Email is optional.
- If supplied, email must have a valid format such as `name@gmail.com`.
- Phone numbers and email addresses must be unique.

The current model treats phone numbers as local 10-digit numbers. It does not have a separate country field or country-specific calling-code validation.

Validation exists in two places:

- `frontend/src/components/ContactForm.vue` gives the user immediate messages.
- `backend/app/schemas.py` enforces the rules on the server.

Existing older database records can still be displayed even if they were saved before the stricter rules were added. If an old record is edited, its new value must follow the current rules.

## 4. Important backend files

### `backend/app/main.py`

Creates the FastAPI application, enables CORS for the frontend, loads the contacts router, and creates database tables when the backend starts.

The line using `Base.metadata.create_all()` means this small project initializes its table automatically. There is no separate migration command currently.

### `backend/app/database.py`

Reads `DATABASE_URL` from `.env`, creates the SQLAlchemy database engine, and provides a database session to each request.

A session is opened for a request and closed afterward. This prevents database connections from being left open.

### `backend/app/models.py`

Defines the PostgreSQL table through the SQLAlchemy `Contact` model.

The table contains:

- `id`: unique number for each contact.
- `name`: contact name.
- `phone_number`: required and unique phone number.
- `email`: optional and unique email.
- `address`: optional address.
- `created_at`: time when the record was created.

### `backend/app/schemas.py`

Defines the shape of incoming and outgoing data using Pydantic.

`ContactCreate` is used for new and updated contacts. Its validators enforce the input rules.

`ContactRead` is used when returning saved contacts. It is deliberately more tolerant so one old invalid record cannot make the entire contact list fail to load.

### `backend/app/crud.py`

Contains database operations:

- Read all contacts.
- Read one contact.
- Create a contact.
- Update a contact.
- Delete a contact.
- Check duplicate phone numbers and emails.
- Convert database errors into useful HTTP errors.

### `backend/app/routes/contacts.py`

Defines the API URLs and connects each URL to a CRUD function.

The available endpoints are:

| Method | URL | Purpose |
|---|---|---|
| `GET` | `/contacts` | Get all contacts |
| `POST` | `/contacts` | Create a contact |
| `GET` | `/contacts/{id}` | Get one contact |
| `PUT` | `/contacts/{id}` | Update a contact |
| `DELETE` | `/contacts/{id}` | Delete a contact |

## 5. Important frontend files

### `frontend/src/main.js`

Starts Vue, installs the router, loads the global CSS, and mounts the app into `index.html`.

### `frontend/src/App.vue`

Provides the common page shell: the header, navigation buttons, page area, and notification area.

### `frontend/src/router/index.js`

Maps browser URLs to Vue pages:

- `/`: contact list.
- `/contacts/new`: create form.
- `/contacts/:id`: contact details.
- `/contacts/:id/edit`: edit form.

### `frontend/src/services/api.js`

Contains the single `fetch()` helper used by the frontend. It sends requests to the backend URL from `VITE_API_BASE_URL` and turns error responses into JavaScript errors.

### `frontend/src/views/ContactListView.vue`

Loads all contacts, shows loading and error states, renders contact cards, and handles deletion.

### `frontend/src/views/ContactDetailView.vue`

Loads and displays one contact and provides edit and delete actions.

### `frontend/src/views/ContactFormView.vue`

Connects the reusable form to either the create or update API request.

### `frontend/src/components/ContactForm.vue`

Displays the form and performs immediate browser-side validation.

### `frontend/src/components/ContactCard.vue`

Displays one contact in the list with view, edit, and delete buttons.

### `frontend/src/styles.css`

Contains the visual styling for the application.

## 6. How Docker is used

Docker packages each part of the system so it can run without installing Python, Node.js, or PostgreSQL directly on Windows.

### `docker-compose.yml`

This is the main Docker configuration. It defines three services:

- `database` uses the `postgres:16-alpine` image.
- `backend` is built from `backend/Dockerfile`.
- `frontend` is built from `frontend/Dockerfile`.

It also maps ports:

- PostgreSQL: host `5432` to container `5432`.
- Backend: host `8000` to container `8000`.
- Frontend: host `5173` to container `5173`.

The backend waits for the database health check before starting. The frontend waits for the backend container to start.

### Backend Dockerfile

`backend/Dockerfile`:

1. Starts with Python 3.12.
2. Installs system packages needed by PostgreSQL Python drivers.
3. Copies `requirements.txt`.
4. Installs FastAPI, SQLAlchemy, Pydantic, and related packages.
5. Copies the backend app.
6. Starts Uvicorn on port 8000.

### Frontend Dockerfile

`frontend/Dockerfile`:

1. Starts with Node 20 Alpine.
2. Copies `package.json`.
3. Runs `npm install`.
4. Copies the Vue source code.
5. Starts Vite on port 5173.

### Database volume

The Compose file defines a volume named `postgres_data`. This volume is where PostgreSQL keeps contact records.

That is why contacts remain after:

```powershell
docker compose down
docker compose up
```

Do not use `docker compose down -v` unless you intentionally want to delete the database volume and all saved contacts.

## 7. Docker commands to run the project

Run every command from the project root:

```powershell
cd C:\Prac-intern\Intern-prac
```

Start for the first time, or after code/dependency changes:

```powershell
docker compose up --build
```

Start in the background:

```powershell
docker compose up -d --build
```

See running services:

```powershell
docker compose ps
```

View logs:

```powershell
docker compose logs -f
```

View only backend logs:

```powershell
docker compose logs -f backend
```

Stop the foreground process with `Ctrl+C`. Then remove the containers while preserving contacts:

```powershell
docker compose down
```

Restart existing containers:

```powershell
docker compose restart
```

Reset everything, including all contacts:

```powershell
docker compose down -v
docker compose up --build
```

Use that last reset command only when you accept permanent data loss.

## 8. Where to access the running system

- Website: http://localhost:5173
- Backend: http://localhost:8000
- Interactive API documentation: http://localhost:8000/docs
- OpenAPI description: http://localhost:8000/openapi.json

The local PostgreSQL database is not a website. It is accessed by the backend through the Docker network.

## 9. Current Git and GitHub situation

The local project already has this GitHub remote configured:

```text
https://github.com/YSawant772005/Intern-prac.git
```

However, the current local branch is `feature/phonebook-app`, it has no commits yet, and the project files are currently untracked. This means the files exist locally and the remote address exists, but the project has not been uploaded to GitHub yet.

The database contacts are also not uploaded to GitHub. They live in the local Docker volume. GitHub stores source files and commit history, not the running database volume.

## 10. Upload the current project to GitHub

Before uploading, make sure `.env` is not staged. It is ignored by `.gitignore` because it can contain passwords and local settings. Upload `.env.example`, not `.env`.

From the project root:

```powershell
git status
git add .
git status
git commit -m "Build phonebook application"
git push -u origin feature/phonebook-app
```

When GitHub asks for authentication, use a GitHub sign-in flow or a Personal Access Token instead of a normal GitHub account password, depending on your Git installation.

After the push, open:

```text
https://github.com/YSawant772005/Intern-prac
```

The branch may be selected from the branch dropdown. If you want the code on the repository's default branch, push it as `main` instead:

```powershell
git branch -M main
git push -u origin main
```

Only use the `main` commands if you are sure you want to rename the current branch.

## 11. Normal Git workflow for future changes

After changing code:

```powershell
git status
git diff
git add path\to\changed-file
git commit -m "Describe the change"
git push
```

To update your local copy before working:

```powershell
git pull --rebase origin feature/phonebook-app
```

If you use `main`, replace `feature/phonebook-app` with `main`.

Useful commands:

```powershell
# Show commit history
git log --oneline --decorate --graph -10

# Show the current branch
git branch --show-current

# Show configured GitHub remote
git remote -v

# See unstaged changes
git diff

# See staged changes
git diff --cached
```

## 12. How to change the latest commit

If you forgot a file or want to correct the latest commit:

```powershell
git add path\to\file
git commit --amend --no-edit
git push
```

If the latest commit was already pushed to GitHub, amending changes its identity. A normal push may be rejected. In that case:

```powershell
git push --force-with-lease
```

Use `--force-with-lease` carefully. It rewrites the remote branch, but it is safer than plain `--force` because Git checks that nobody else pushed new work first.

To change the latest commit message only:

```powershell
git commit --amend -m "Better commit message"
```

## 13. How to change an older commit

First inspect the history:

```powershell
git log --oneline --decorate --graph
```

To edit one of the last five commits:

```powershell
git rebase -i HEAD~5
```

An editor opens with a list of commits. Change `pick` to one of these words:

- `reword`: change only the commit message.
- `edit`: pause at that commit so you can change its files.
- `squash`: combine it with the commit above it.
- `drop`: remove that commit.

For an `edit` operation:

```powershell
git reset HEAD^
# edit the files
git add .
git commit --amend
git rebase --continue
```

If conflicts appear:

```powershell
git status
# fix the files marked as conflicted
git add path\to\fixed-file
git rebase --continue
```

To cancel the rebase and return to the previous state:

```powershell
git rebase --abort
```

If those commits were already pushed to GitHub, finish with:

```powershell
git push --force-with-lease
```

Rewriting old commits changes history. Avoid it on a shared branch unless everyone agrees.

## 14. Safer alternative to changing old public commits

If the project is already shared or the old commit is already on GitHub, it is usually safer to make a new correcting commit:

```powershell
# edit the required files
git add .
git commit -m "Correct contact validation"
git push
```

This keeps the existing history intact and is the recommended choice for normal team work.

## 15. Recommended change-and-test routine

For a code change:

1. Start the services with `docker compose up --build`.
2. Change the relevant source file.
3. Rebuild if the changed code is copied into an image.
4. Test the behavior in the browser.
5. Check API behavior at `/docs` if the backend changed.
6. Check logs with `docker compose logs backend` if something fails.
7. Run `git diff` and inspect the changes.
8. Commit and push only the intended files.

For this project, a frontend production build can be checked with:

```powershell
docker compose run --build --rm frontend npm run build
```

A backend schema check can be run with:

```powershell
docker compose run --build --rm backend python -c "from app.schemas import ContactCreate; print(ContactCreate(name='Test User', phone_number='9876543210', email='test@gmail.com'))"
```

## 16. Short version

To run the project:

```powershell
cd C:\Prac-intern\Intern-prac
docker compose up --build
```

Open http://localhost:5173.

To upload the current code to GitHub for the first time:

```powershell
git add .
git commit -m "Build phonebook application"
git push -u origin feature/phonebook-app
```

To make future changes:

```powershell
git add .
git commit -m "Describe the change"
git push
```

The current local project has no previous commits yet, so there are no older commits to edit at this moment. Once commits exist, use `git commit --amend` for the latest commit or `git rebase -i` for older commits.

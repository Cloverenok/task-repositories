# GitHub Repositories API

Spring Boot application that acts as a proxy to the GitHub API, exposing an endpoint to list non-fork repositories of a given user along with their branches and last commit SHA.

## Stack

- Java 25
- Spring Boot 4
- Gradle (Kotlin DSL)
- WireMock (integration tests)

## Running the application

```bash
./gradlew bootRun
```

The application starts on port `8080`.

## API

### Get repositories for a user

```
GET /api/repositories/{username}
```

Returns all non-fork repositories of the given GitHub user. For each repository, the response includes the repository name, owner login, and a list of branches with their last commit SHA.

**Example response:**

```json
[
  {
    "repositoryName": "task-repositories",
    "ownerLogin": "Cloverenok",
    "branches": [
      {
        "name": "master",
        "lastCommitSha": "0d0a359a84f0c2c145ee80f019702fb1e59d2300"
      }
    ]
  }
]
```

### User not found

If the given username does not exist on GitHub, the API returns: **HTTP 404**


```json
{
  "status": 404,
  "message": "User not found: {username}"
}
```

**Example response:**

```json
[
  {
    "status": 404,
    "message": "User not found: cloverenok22"
  }
]
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `github.api.url` | `https://api.github.com` | Base URL of the GitHub API |

## Running tests

```bash
./gradlew test
```
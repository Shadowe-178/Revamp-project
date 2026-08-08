# SmartChess V2 Professional Development Specification

SmartChess is a Java 17 Spring Boot application for Chinese Chess and Dark Chess.

## Spec-driven feature map
The project is organized around the V2 development spec and currently wires the frontend through these entry points:

- Chinese Chess complete rules
- Dark Chess complete rules
- Two-player match flow
- AI opponent flow with level-based analysis hooks
- Undo / restart / logout navigation
- Replay / history / ranking / profile area
- Save/load game state support and LocalStorage-ready browser-side behavior
- Responsive interface

## Project Structure
- `.github/workflows/build.yml`
- `docs/`
- `backend/`
- `frontend/`

## Backend Structure
- `backend/pom.xml`
- `backend/src/main/java/com/example/smartchess`
- `backend/src/main/resources/templates`
- `backend/src/main/resources/static`

## Current page wiring
- `/` → landing page with game entry and account navigation
- `/chess` → Chinese Chess board page
- `/dark` → Dark Chess board page
- `/replay` → replay viewer page
- `/ranking` → ranking page
- `/history` → history page
- `/profile` → profile page
- `/logout` → logout action

## Run Backend
```bash
cd backend
mvn clean package
mvn spring-boot:run
```

## Notes
This scaffold contains the main application routes, templates, and API entry points needed to connect the V2 specification to the Spring Boot backend.

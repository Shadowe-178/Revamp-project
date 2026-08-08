# SmartChess Database Design

## 1. Tables and separation

### player
- `id` (PK)
- `username`
- `password`
- `display_name`
- `total_games`
- `win_count`
- `chess_games`
- `dark_games`

### chinese chess
- `chess_match`
- `chess_board`
- `chess_move_record`

### dark chess
- `dark_match`
- `dark_board`
- `dark_move_record`

## 2. Logical separation
- `player` is the shared user table.
- `chess_*` tables store standard Chinese Chess matches, board snapshots, and move records.
- `dark_*` tables store Dark Chess matches, board states, and move records.
- Each game module is completely separated in both the backend package and database schema naming.

## 3. Notes
- The current backend uses H2 in-memory database for development.
- Production can be switched to a dedicated relational database without changing the logical model.

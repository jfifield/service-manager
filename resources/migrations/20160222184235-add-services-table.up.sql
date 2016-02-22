CREATE TABLE IF NOT EXISTS services (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  start_command TEXT NOT NULL,
  stop_command TEXT NOT NULL,
  status_command TEXT NOT NULL
);

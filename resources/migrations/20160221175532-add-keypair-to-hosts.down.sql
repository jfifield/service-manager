DROP TABLE IF EXISTS hosts;
--;;
CREATE TABLE IF NOT EXISTS hosts (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  address TEXT NOT NULL,
  username TEXT NOT NULL
);

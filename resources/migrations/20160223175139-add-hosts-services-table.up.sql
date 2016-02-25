CREATE TABLE IF NOT EXISTS hosts_services (
  host_id INTEGER NOT NULL REFERENCES hosts,
  service_id INTEGER NOT NULL REFERENCES services,
  PRIMARY KEY (host_id, service_id)
);

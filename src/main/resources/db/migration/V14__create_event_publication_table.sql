CREATE TABLE IF NOT EXISTS event_publication
(
    id               VARCHAR(36) NOT NULL,
    listener_id      VARCHAR(512) NOT NULL,
    event_type       VARCHAR(512) NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL,
    publication_date TIMESTAMP(6) NOT NULL,
    completion_date  TIMESTAMP(6) DEFAULT NULL,
    PRIMARY KEY (id)
);
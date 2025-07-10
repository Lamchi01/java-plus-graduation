CREATE TABLE IF NOT EXISTS events_similarity (
 event_a BIGINT,
 event_b BIGINT,
 score NUMERIC(3, 2) NOT NULL,
 action_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
 PRIMARY KEY (event_a, event_b)
);

CREATE TABLE IF NOT EXISTS users_actions (
 user_id BIGINT NOT NULL,
 event_id BIGINT NOT NULL,
 weight NUMERIC(3, 2) NOT NULL,
 last_action_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
 PRIMARY KEY (event_id, user_id)
);
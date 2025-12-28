CREATE TABLE IF NOT EXISTS thread (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id BIGINT UNIQUE NOT NULL,
    created timestamptz NOT NULL
);
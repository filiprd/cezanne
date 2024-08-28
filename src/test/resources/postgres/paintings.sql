CREATE TABLE IF NOT EXISTS paintings (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    technique TEXT NOT NULL,
    images TEXT[],
    price INTEGER NOT NULL,
    user_id BIGINT NOT NULL
);
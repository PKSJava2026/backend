-- Схема БД экспедиторской компании (PostgreSQL). Скрипт идемпотентен: выполняется при каждом запуске.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE
    IF NOT EXISTS users (
        id bigserial PRIMARY KEY,
        nickname varchar(30) NOT NULL,
        email varchar(150) NOT NULL UNIQUE,
        password_hash varchar NOT NULL,
        role varchar NOT NULL CHECK (
            role IN ('CUSTOMER', 'FORWARDER', 'CARRIER', 'ADMIN')
        ),
        is_active boolean NOT NULL DEFAULT true,
        created_at timestamptz NOT NULL DEFAULT now ()
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_nickname_lower ON users (lower(nickname));

CREATE TABLE
    IF NOT EXISTS orders (
        id bigserial PRIMARY KEY,
        customer_id bigint NOT NULL REFERENCES users (id),
        cargo_description text NOT NULL,
        weight_kg numeric CHECK (weight_kg >= 0),
        volume_m3 numeric CHECK (volume_m3 >= 0),
        origin varchar NOT NULL,
        destination varchar NOT NULL,
        desired_date date,
        status varchar NOT NULL DEFAULT 'NEW' CHECK (
            status IN (
                'NEW',
                'CONTRACTED',
                'IN_TRANSIT',
                'DELIVERED',
                'CANCELLED'
            )
        ),
        created_at timestamptz NOT NULL DEFAULT now ()
    );

CREATE TABLE
    IF NOT EXISTS cooperation_requests (
        id bigserial PRIMARY KEY,
        carrier_id bigint NOT NULL REFERENCES users (id),
        description text,
        vehicle_info text,
        status varchar NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'PROCESSED', 'CANCELLED')),
        created_at timestamptz NOT NULL DEFAULT now ()
    );

CREATE TABLE
    IF NOT EXISTS customer_contracts (
        id bigserial PRIMARY KEY,
        order_id bigint NOT NULL REFERENCES orders (id),
        customer_id bigint NOT NULL REFERENCES users (id),
        forwarder_id bigint NOT NULL REFERENCES users (id),
        price numeric(12, 2) NOT NULL CHECK (price >= 0),
        terms text,
        start_date date,
        end_date date,
        status varchar NOT NULL DEFAULT 'PENDING' CHECK (
            status IN (
                'PENDING',
                'ACTIVE',
                'TERMINATED',
                'COMPLETED',
                'REJECTED'
            )
        ),
        created_by bigint NOT NULL REFERENCES users (id),
        created_at timestamptz NOT NULL DEFAULT now (),
        CHECK (
            end_date IS NULL
            OR start_date IS NULL
            OR end_date >= start_date
        )
    );

CREATE TABLE
    IF NOT EXISTS carrier_contracts (
        id bigserial PRIMARY KEY,
        order_id bigint NOT NULL REFERENCES orders (id),
        cooperation_id bigint REFERENCES cooperation_requests (id),
        carrier_id bigint NOT NULL REFERENCES users (id),
        forwarder_id bigint NOT NULL REFERENCES users (id),
        price numeric(12, 2) NOT NULL CHECK (price >= 0),
        terms text,
        start_date date,
        end_date date,
        status varchar NOT NULL DEFAULT 'PENDING' CHECK (
            status IN (
                'PENDING',
                'ACTIVE',
                'TERMINATED',
                'COMPLETED',
                'REJECTED'
            )
        ),
        created_by bigint NOT NULL REFERENCES users (id),
        created_at timestamptz NOT NULL DEFAULT now (),
        CHECK (
            end_date IS NULL
            OR start_date IS NULL
            OR end_date >= start_date
        )
    );

CREATE TABLE
    IF NOT EXISTS contract_change_requests (
        id bigserial PRIMARY KEY,
        customer_contract_id bigint REFERENCES customer_contracts (id),
        carrier_contract_id bigint REFERENCES carrier_contracts (id),
        type varchar NOT NULL CHECK (type IN ('AMEND', 'TERMINATE')),
        initiated_by bigint NOT NULL REFERENCES users (id),
        proposed_price numeric(12, 2) CHECK (proposed_price >= 0),
        proposed_terms text,
        proposed_start_date date,
        proposed_end_date date,
        status varchar NOT NULL DEFAULT 'PENDING' CHECK (
            status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')
        ),
        created_at timestamptz NOT NULL DEFAULT now (),
        resolved_at timestamptz,
        CHECK (
            num_nonnulls (customer_contract_id, carrier_contract_id) = 1
        ),
        CHECK (
            type = 'AMEND'
            OR (
                proposed_price IS NULL
                AND proposed_terms IS NULL
                AND proposed_start_date IS NULL
                AND proposed_end_date IS NULL
            )
        )
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_ccr_open_customer ON contract_change_requests (customer_contract_id)
WHERE
    status = 'PENDING'
    AND customer_contract_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_ccr_open_carrier ON contract_change_requests (carrier_contract_id)
WHERE
    status = 'PENDING'
    AND carrier_contract_id IS NOT NULL;

CREATE TABLE
    IF NOT EXISTS ratings (
        id bigserial PRIMARY KEY,
        order_id bigint NOT NULL REFERENCES orders (id),
        from_user_id bigint NOT NULL REFERENCES users (id),
        to_user_id bigint NOT NULL REFERENCES users (id),
        score smallint NOT NULL CHECK (score BETWEEN 1 AND 5),
        comment text,
        created_at timestamptz NOT NULL DEFAULT now (),
        UNIQUE (order_id, from_user_id),
        CHECK (from_user_id <> to_user_id)
    );

CREATE TABLE
    IF NOT EXISTS notifications (
        id bigserial PRIMARY KEY,
        user_id bigint NOT NULL REFERENCES users (id),
        change_request_id bigint REFERENCES contract_change_requests (id),
        message text NOT NULL,
        is_read boolean NOT NULL DEFAULT false,
        created_at timestamptz NOT NULL DEFAULT now ()
    );

INSERT INTO
    users (nickname, email, password_hash, role)
VALUES
    (
        'admin',
        'admin@expedition.local',
        crypt ('admin', gen_salt ('bf')),
        'ADMIN'
    ) ON CONFLICT DO NOTHING;
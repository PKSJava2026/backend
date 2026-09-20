CREATE TABLE
    roles (
              role_id smallserial PRIMARY KEY,
              name varchar NOT NULL UNIQUE CHECK (
                  name IN ('CUSTOMER', 'FORWARDER', 'CARRIER', 'ADMIN')
                  )
);

INSERT INTO
    roles (name)
VALUES
    ('CUSTOMER'),
    ('FORWARDER'),
    ('CARRIER'),
    ('ADMIN');

CREATE TABLE
    users (
              user_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
              role_id smallint NOT NULL REFERENCES roles (role_id),
              email varchar NOT NULL UNIQUE,
              password_hash varchar NOT NULL,
              full_name varchar NOT NULL,
              phone varchar,
              company_name varchar,
              is_active boolean NOT NULL DEFAULT true,
              created_at timestamptz NOT NULL DEFAULT now ()
);

CREATE INDEX idx_users_role ON users (role_id);

CREATE TABLE
    orders (
               order_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
               customer_id uuid NOT NULL REFERENCES users (user_id),
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

CREATE INDEX idx_orders_customer_created ON orders (customer_id, created_at DESC);

CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE
    carrier_applications (
                             application_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
                             carrier_id uuid NOT NULL REFERENCES users (user_id),
                             description text,
                             vehicle_info text,
                             status varchar NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'PROCESSED', 'CANCELLED')),
                             created_at timestamptz NOT NULL DEFAULT now ()
);

CREATE INDEX idx_carrier_apps_carrier ON carrier_applications (carrier_id);

CREATE INDEX idx_carrier_apps_status ON carrier_applications (status);

CREATE TABLE
    customer_contracts (
                           contract_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
                           order_id uuid NOT NULL REFERENCES orders (order_id),
                           customer_id uuid NOT NULL REFERENCES users (user_id),
                           forwarder_id uuid NOT NULL REFERENCES users (user_id),
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
                           created_by uuid NOT NULL REFERENCES users (user_id),
                           created_at timestamptz NOT NULL DEFAULT now (),
                           CHECK (
                               end_date IS NULL
                                   OR start_date IS NULL
                                   OR end_date >= start_date
                               )
);

CREATE INDEX idx_cc_order ON customer_contracts (order_id);

CREATE INDEX idx_cc_customer ON customer_contracts (customer_id);

CREATE INDEX idx_cc_forwarder ON customer_contracts (forwarder_id, status, created_at DESC);

CREATE TABLE
    carrier_contracts (
                          contract_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
                          order_id uuid NOT NULL REFERENCES orders (order_id),
                          application_id uuid REFERENCES carrier_applications (application_id),
                          carrier_id uuid NOT NULL REFERENCES users (user_id),
                          forwarder_id uuid NOT NULL REFERENCES users (user_id),
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
                          created_by uuid NOT NULL REFERENCES users (user_id),
                          created_at timestamptz NOT NULL DEFAULT now (),
                          CHECK (
                              end_date IS NULL
                                  OR start_date IS NULL
                                  OR end_date >= start_date
                              )
);

CREATE INDEX idx_crc_order ON carrier_contracts (order_id);

CREATE INDEX idx_crc_carrier ON carrier_contracts (carrier_id);

CREATE INDEX idx_crc_forwarder ON carrier_contracts (forwarder_id, status, created_at DESC);

CREATE INDEX idx_crc_app ON carrier_contracts (application_id);

CREATE TABLE
    contract_change_requests (
                                 request_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
                                 customer_contract_id uuid REFERENCES customer_contracts (contract_id),
                                 carrier_contract_id uuid REFERENCES carrier_contracts (contract_id),
                                 type varchar NOT NULL CHECK (type IN ('AMEND', 'TERMINATE')),
                                 initiated_by uuid NOT NULL REFERENCES users (user_id),
                                 proposed_changes jsonb,
                                 previous_values jsonb,
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
                                         OR proposed_changes IS NULL
                                     )
);

CREATE INDEX idx_ccr_customer_contract ON contract_change_requests (customer_contract_id);

CREATE INDEX idx_ccr_carrier_contract ON contract_change_requests (carrier_contract_id);

CREATE INDEX idx_ccr_initiator ON contract_change_requests (initiated_by);

CREATE UNIQUE INDEX uq_ccr_open_customer ON contract_change_requests (customer_contract_id)
    WHERE
    status = 'PENDING'
    AND customer_contract_id IS NOT NULL;

CREATE UNIQUE INDEX uq_ccr_open_carrier ON contract_change_requests (carrier_contract_id)
    WHERE
    status = 'PENDING'
    AND carrier_contract_id IS NOT NULL;

CREATE TABLE
    ratings (
                rating_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
                order_id uuid NOT NULL REFERENCES orders (order_id),
                from_user_id uuid NOT NULL REFERENCES users (user_id),
                to_user_id uuid NOT NULL REFERENCES users (user_id),
                score smallint NOT NULL CHECK (score BETWEEN 1 AND 5),
                comment text,
                created_at timestamptz NOT NULL DEFAULT now (),
                UNIQUE (order_id, from_user_id),
                CHECK (from_user_id <> to_user_id)
);

CREATE INDEX idx_ratings_to_user ON ratings (to_user_id);

CREATE TABLE
    notifications (
                      notification_id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
                      user_id uuid NOT NULL REFERENCES users (user_id),
                      type varchar NOT NULL,
                      request_id uuid REFERENCES contract_change_requests (request_id),
                      message text,
                      is_read boolean NOT NULL DEFAULT false,
                      created_at timestamptz NOT NULL DEFAULT now ()
);

CREATE INDEX idx_notifications_user ON notifications (user_id, is_read, created_at DESC);
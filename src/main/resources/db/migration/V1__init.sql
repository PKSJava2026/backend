CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'CARRIER', 'FORWARDER', 'ADMIN'))
);

CREATE TABLE shipment_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    cargo_description VARCHAR(255) NOT NULL,
    origin VARCHAR(150) NOT NULL,
    destination VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('NEW', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    price NUMERIC(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO users (full_name, email, phone, role) VALUES
    ('Иван Петров', 'ivan@example.com', '+79001234567', 'CUSTOMER'),
    ('Ольга Смирнова', 'olga@example.com', '+79001234568', 'CUSTOMER'),
    ('ООО Быстрая доставка', 'fast@example.com', '+79001234569', 'CARRIER'),
    ('ИП Грузоперевозки', 'cargo@example.com', '+79001234570', 'CARRIER'),
    ('Мария Экспедитор', 'maria@example.com', '+79001234571', 'FORWARDER');

INSERT INTO shipment_requests (user_id, cargo_description, origin, destination, status, price) VALUES
    (1, 'Мебель', 'Москва', 'Санкт-Петербург', 'NEW', 15000.00),
    (1, 'Стройматериалы', 'Москва', 'Казань', 'IN_PROGRESS', 22000.00),
    (2, 'Электроника', 'Екатеринбург', 'Москва', 'COMPLETED', 18500.00),
    (2, 'Продукты питания', 'Новосибирск', 'Омск', 'CANCELLED', 9000.00),
    (1, 'Одежда', 'Москва', 'Воронеж', 'NEW', 7000.00),
    (2, 'Автозапчасти', 'Самара', 'Уфа', 'IN_PROGRESS', 12500.00),
    (1, 'Мебель', 'Санкт-Петербург', 'Москва', 'COMPLETED', 16000.00),
    (2, 'Оборудование', 'Москва', 'Ростов-на-Дону', 'NEW', 30000.00),
    (1, 'Книги', 'Москва', 'Тверь', 'NEW', 4000.00),
    (2, 'Химия бытовая', 'Пермь', 'Ижевск', 'CANCELLED', 8000.00);
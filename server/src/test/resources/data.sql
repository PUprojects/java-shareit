INSERT INTO users (id, name, email) VALUES (1, 'First user', 'first@mail.ru');
INSERT INTO users (id, name, email) VALUES (2, 'Second user', 'second@mail.ru');

INSERT INTO items (id, name, description, available, owner_id, request_id) VALUES
    (1, 'First item', 'First item desc', TRUE, 2, NULL);
